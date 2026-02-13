package com.example.bloodchain.service;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

/**
 * Service for managing hospital blood requests.
 * Implements BloodunitRequested() and OrderApproval() from IEEE paper.
 */
@Service
public class BloodRequestService {

    @Autowired
    private BloodRequestRepository bloodRequestRepository;

    @Autowired
    private BloodComponentRepository componentRepository;

    @Autowired
    private BloodRequestComponentRepository requestComponentRepository;

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private BloodTrackingService trackingService;

    @Autowired
    private BloodComponentService componentService;

    /**
     * Create a new blood request from hospital.
     */
    @Transactional
    public BloodRequest createBloodRequest(BloodRequest request) {
        // Generate unique request number
        request.setRequestNumber(generateUniqueRequestNumber());
        request.setRequestDate(LocalDateTime.now());
        request.setStatus(BloodRequestStatus.REQUESTED);
        
        return bloodRequestRepository.save(request);
    }

    /**
     * Approve blood request and assign components.
     */
    @Transactional
    public void approveBloodRequest(Long requestId, String approvedBy) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (request.getStatus() != BloodRequestStatus.REQUESTED) {
            throw new IllegalStateException("Only REQUESTED requests can be approved");
        }
        
        // Default component type to WHOLE_BLOOD if null (for old records)
        final ComponentType typeToFind = (request.getComponentType() != null) ? 
            request.getComponentType() : ComponentType.WHOLE_BLOOD;

        int qtyRequested = request.getQuantity() != null ? request.getQuantity() : 1;

        // Find available components
        List<BloodComponent> availableComponents = componentService.findAvailableComponents(
            typeToFind,
            request.getBloodGroup(),
            qtyRequested
        );
        
        if (availableComponents.isEmpty()) {
            // Provide detailed error message
            long totalOfType = componentRepository.countByComponentType(typeToFind);
            long availableOfType = componentRepository.countAvailableComponents(typeToFind, request.getBloodGroup(), LocalDate.now());
            
            String errorMsg = String.format(
                "No available %s components found for blood group %s. " +
                "Total in system: %d, Available: %d. " +
                "Please check if donations have been completed and components separated.",
                typeToFind, request.getBloodGroup(), totalOfType, availableOfType
            );
            throw new RuntimeException(errorMsg);
        }
        
        int approvedQuantity = Math.min(availableComponents.size(), request.getQuantity());
        
        // Assign components to request
        for (int i = 0; i < approvedQuantity; i++) {
            BloodComponent component = availableComponents.get(i);
            
            // Reserve the component
            componentService.updateComponentStatus(
                component.getId(),
                ComponentStatus.RESERVED,
                approvedBy,
                "ROLE_BLOODBANK"
            );
            
            // Create mapping
            BloodRequestComponent mapping = new BloodRequestComponent();
            mapping.setBloodRequestId(request.getId());
            mapping.setBloodComponentId(component.getId());
            mapping.setAssignedDate(LocalDateTime.now());
            requestComponentRepository.save(mapping);

            // Update parent BloodUnit status to APPROVED (deducts from STORED inventory)
            bloodUnitRepository.findById(component.getBloodUnitId()).ifPresent(unit -> {
                unit.setStatus(BloodUnitStatus.APPROVED);
                bloodUnitRepository.save(unit);

                // If this was a Whole Blood request, also reserve all other components of this unit
                if (typeToFind == ComponentType.WHOLE_BLOOD) {
                    List<BloodComponent> allComponents = componentRepository.findByBloodUnitId(unit.getId());
                    for (BloodComponent other : allComponents) {
                        if (other.getStatus() == ComponentStatus.AVAILABLE) {
                            other.setStatus(ComponentStatus.RESERVED);
                            componentRepository.save(other);
                        }
                    }
                }
            });
        }
        
        // Update request status
        request.setStatus(approvedQuantity == request.getQuantity() ? 
            BloodRequestStatus.APPROVED : BloodRequestStatus.PARTIALLY_APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedDate(LocalDateTime.now());
        request.setApprovedQuantity(approvedQuantity);
        
        bloodRequestRepository.save(request);
    }

    /**
     * Reject blood request.
     */
    @Transactional
    public void rejectBloodRequest(Long requestId, String rejectionReason, String approvedBy) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        request.setStatus(BloodRequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        request.setApprovedBy(approvedBy);
        request.setApprovedDate(LocalDateTime.now());
        
        bloodRequestRepository.save(request);
    }

    /**
     * Dispatch blood to hospital.
     */
    @Transactional
    public void dispatchBlood(Long requestId, String performedBy) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (request.getStatus() != BloodRequestStatus.APPROVED && 
            request.getStatus() != BloodRequestStatus.PARTIALLY_APPROVED) {
            throw new IllegalStateException("Only APPROVED requests can be dispatched");
        }
        
        // Update component mappings
        List<BloodRequestComponent> mappings = requestComponentRepository
            .findByBloodRequestId(requestId);
        
        for (BloodRequestComponent mapping : mappings) {
            mapping.setDispatchDate(LocalDateTime.now());
            requestComponentRepository.save(mapping);
            
            // Update component status
            componentService.updateComponentStatus(
                mapping.getBloodComponentId(),
                ComponentStatus.DISPATCHED,
                performedBy,
                "ROLE_BLOODBANK"
            );
        }
        
        // Update request status
        request.setStatus(BloodRequestStatus.DISPATCHED);
        bloodRequestRepository.save(request);
    }

    /**
     * Confirm receipt of blood by hospital.
     */
    @Transactional
    public void confirmReceipt(Long requestId, String performedBy) {
        BloodRequest request = bloodRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Blood request not found"));
        
        if (request.getStatus() != BloodRequestStatus.DISPATCHED) {
            throw new IllegalStateException("Only DISPATCHED requests can be received");
        }
        
        // Update component mappings
        List<BloodRequestComponent> mappings = requestComponentRepository
            .findByBloodRequestId(requestId);
        
        for (BloodRequestComponent mapping : mappings) {
            mapping.setReceivedDate(LocalDateTime.now());
            requestComponentRepository.save(mapping);
            
            // Get component and blood unit for logging
            BloodComponent component = componentRepository.findById(mapping.getBloodComponentId())
                .orElse(null);
            
            if (component != null) {
                // Update component status to RECEIVED (Hospital Inventory)
                componentService.updateComponentStatus(
                    component.getId(),
                    ComponentStatus.RECEIVED,
                    performedBy,
                    "ROLE_HOSPITAL"
                );

                BloodUnit unit = bloodUnitRepository.findById(component.getBloodUnitId())
                    .orElse(null);
                
                if (unit != null) {
                    // Update blood unit status
                    unit.setStatus(BloodUnitStatus.RECEIVED);
                    bloodUnitRepository.save(unit);
                    
                    // Log the receipt
                    trackingService.logStatusChange(
                        unit.getBloodUnitId(),
                        "BLOOD_RECEIVED",
                        performedBy,
                        "ROLE_HOSPITAL",
                        BloodUnitStatus.DISPATCHED.toString(),
                        BloodUnitStatus.RECEIVED.toString(),
                        String.format("Received by hospital for request: %s", request.getRequestNumber())
                    );
                }
            }
        }
        
        // Update request status
        request.setStatus(BloodRequestStatus.COMPLETED);
        bloodRequestRepository.save(request);
    }

    /**
     * Find pending blood requests (for blood bank dashboard).
     */
    public List<BloodRequest> findPendingRequests() {
        return bloodRequestRepository.findByStatusOrderByUrgencyDescRequestDateAsc(
            BloodRequestStatus.REQUESTED
        );
    }

    /**
     * Find all blood requests by hospital email.
     */
    public List<BloodRequest> findByHospitalEmail(String hospitalEmail) {
        return bloodRequestRepository.findByHospitalEmailOrderByRequestDateDesc(hospitalEmail);
    }

    /**
     * Generate unique request number.
     * Format: REQ-YYYY-NNNNNN
     */
    private String generateUniqueRequestNumber() {
        int year = Year.now().getValue();
        long count = bloodRequestRepository.count() + 1;
        String requestNumber;
        
        do {
            requestNumber = String.format("REQ-%d-%06d", year, count++);
        } while (bloodRequestRepository.existsByRequestNumber(requestNumber));
        
        return requestNumber;
    }
}
