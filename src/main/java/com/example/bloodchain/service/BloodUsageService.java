package com.example.bloodchain.service;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing blood usage (patient transfusions).
 * Implements BloodUnitPrescription() and BloodUnitTransfusion() from IEEE paper.
 */
@Service
public class BloodUsageService {

    @Autowired
    private BloodUsageRepository bloodUsageRepository;

    @Autowired
    private BloodComponentRepository componentRepository;

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private BloodTrackingService trackingService;

    /**
     * Record blood transfusion to patient.
     */
    @Transactional
    public BloodUsage recordTransfusion(BloodUsage usage, String performedBy) {
        // Validate component exists and is available
        BloodComponent component = componentRepository.findById(usage.getBloodComponentId())
            .orElseThrow(() -> new RuntimeException("Blood component not found"));
        
        if (component.getStatus() != ComponentStatus.DISPATCHED && 
            component.getStatus() != ComponentStatus.RESERVED) {
            throw new IllegalStateException(
                "Component must be DISPATCHED or RESERVED to be used. Current status: " + component.getStatus()
            );
        }
        
        // Set transfusion date
        usage.setTransfusionDate(LocalDateTime.now());
        
        // Save usage record
        BloodUsage savedUsage = bloodUsageRepository.save(usage);
        
        // Update component status to USED
        component.setStatus(ComponentStatus.USED);
        componentRepository.save(component);
        
        // Update blood unit status to USED
        BloodUnit unit = bloodUnitRepository.findById(component.getBloodUnitId())
            .orElseThrow(() -> new RuntimeException("Blood unit not found"));
        
        unit.setStatus(BloodUnitStatus.USED);
        bloodUnitRepository.save(unit);
        
        // Log the transfusion
        trackingService.logStatusChange(
            unit.getBloodUnitId(),
            "BLOOD_TRANSFUSED",
            performedBy,
            "ROLE_HOSPITAL",
            BloodUnitStatus.RECEIVED.toString(),
            BloodUnitStatus.USED.toString(),
            String.format("Transfused to patient: %s (Age: %d, Blood Group: %s). Component: %s. Doctor: %s, Nurse: %s. Reason: %s",
                usage.getPatientName(),
                usage.getPatientAge(),
                usage.getPatientBloodGroup(),
                component.getComponentType(),
                usage.getDoctorName(),
                usage.getNurseName(),
                usage.getTransfusionReason())
        );
        
        // If adverse reaction, log it
        if (usage.getAdverseReaction() != AdverseReaction.NONE) {
            trackingService.logAction(
                unit.getBloodUnitId(),
                "ADVERSE_REACTION",
                performedBy,
                "ROLE_HOSPITAL",
                String.format("Adverse reaction: %s. Details: %s",
                    usage.getAdverseReaction(),
                    usage.getReactionDetails())
            );
        }
        
        return savedUsage;
    }

    /**
     * Find all usage records for a hospital.
     */
    public List<BloodUsage> findByHospitalId(Integer hospitalId) {
        return bloodUsageRepository.findByHospitalIdOrderByTransfusionDateDesc(hospitalId);
    }

    /**
     * Find usage record for a specific component.
     */
    public List<BloodUsage> findByComponentId(Long componentId) {
        return bloodUsageRepository.findByBloodComponentId(componentId);
    }

    /**
     * Get usage analytics for the last X days.
     * Returns a Map of Date (String) to Count (Long).
     */
    public java.util.Map<String, Long> getUsageAnalytics(Integer hospitalId, int days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days).withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<BloodUsage> usageHistory = bloodUsageRepository.findByTransfusionDateBetweenOrderByTransfusionDateDesc(
            startDate, LocalDateTime.now()
        );

        // Filter by hospital and group by date
        java.util.Map<String, Long> analytics = new java.util.TreeMap<>(); // TreeMap to keep dates sorted
        
        // Initialize with 0 for all days in range
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 0; i < days; i++) {
            analytics.put(today.minusDays(i).toString(), 0L);
        }

        usageHistory.stream()
            .filter(u -> u.getHospitalId().equals(hospitalId))
            .forEach(u -> {
                String date = u.getTransfusionDate().toLocalDate().toString();
                analytics.put(date, analytics.getOrDefault(date, 0L) + 1);
            });

        return analytics;
    }
}
