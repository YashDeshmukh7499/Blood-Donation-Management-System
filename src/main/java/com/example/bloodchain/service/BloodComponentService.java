package com.example.bloodchain.service;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.BloodComponentRepository;
import com.example.bloodchain.repository.BloodUnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing blood components.
 * Implements component separation technique from IEEE paper.
 */
@Service
public class BloodComponentService {

    @Autowired
    private BloodComponentRepository componentRepository;

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private BloodTrackingService trackingService;

    /**
     * Separate whole blood into components (RBC, Plasma, Platelets).
     * This is a KEY TECHNIQUE from the IEEE paper.
     * Each component has different expiry dates and storage requirements.
     */
    @Transactional
    public List<BloodComponent> separateComponents(Long bloodUnitId, String performedBy) {
        BloodUnit unit = bloodUnitRepository.findById(bloodUnitId)
            .orElseThrow(() -> new RuntimeException("Blood unit not found"));
        
        if (unit.getStatus() != BloodUnitStatus.TESTED) {
            throw new IllegalStateException("Blood must be TESTED before separation");
        }
        
        if (unit.getTestStatus() != TestStatus.PASSED) {
            throw new IllegalStateException("Blood must PASS all tests before separation");
        }
        
        List<BloodComponent> components = new ArrayList<>();
        LocalDate separationDate = LocalDate.now();
        ComponentType donationType = unit.getDonationType();
        if (donationType == null) donationType = ComponentType.WHOLE_BLOOD;
        
        // RBC Component
        if (donationType == ComponentType.WHOLE_BLOOD || donationType == ComponentType.RBC) {
            BloodComponent rbc = new BloodComponent();
            rbc.setComponentId("RBC-" + unit.getId());
            rbc.setBloodUnitId(unit.getId());
            rbc.setComponentType(ComponentType.RBC);
            rbc.setSeparationDate(separationDate);
            rbc.setExpiryDate(separationDate.plusDays(42));
            rbc.setStatus(ComponentStatus.AVAILABLE);
            rbc.setVolumeMl(200);
            rbc.setStorageType("Refrigerator (2-6°C)");
            rbc.setStorageTemperature(4.0);
            components.add(rbc);
        }
        
        // Plasma Component
        if (donationType == ComponentType.WHOLE_BLOOD || donationType == ComponentType.PLASMA) {
            BloodComponent plasma = new BloodComponent();
            plasma.setComponentId("PLASMA-" + unit.getId());
            plasma.setBloodUnitId(unit.getId());
            plasma.setComponentType(ComponentType.PLASMA);
            plasma.setSeparationDate(separationDate);
            plasma.setExpiryDate(separationDate.plusYears(1));
            plasma.setStatus(ComponentStatus.AVAILABLE);
            plasma.setVolumeMl(200);
            plasma.setStorageType("Freezer (-18°C)");
            plasma.setStorageTemperature(-18.0);
            components.add(plasma);
        }
        
        // Platelets Component
        if (donationType == ComponentType.WHOLE_BLOOD || donationType == ComponentType.PLATELETS) {
            BloodComponent platelets = new BloodComponent();
            platelets.setComponentId("PLT-" + unit.getId());
            platelets.setBloodUnitId(unit.getId());
            platelets.setComponentType(ComponentType.PLATELETS);
            platelets.setSeparationDate(separationDate);
            platelets.setExpiryDate(separationDate.plusDays(5));
            platelets.setStatus(ComponentStatus.AVAILABLE);
            platelets.setVolumeMl(50);
            platelets.setStorageType("Room Temperature (20-24°C)");
            platelets.setStorageTemperature(22.0);
            components.add(platelets);
        }
        
        // Whole Blood Component (Always available for WHOLE_BLOOD, or specifically if requested)
        if (donationType == ComponentType.WHOLE_BLOOD) {
            BloodComponent wholeBlood = new BloodComponent();
            wholeBlood.setComponentId("WB-" + unit.getId());
            wholeBlood.setBloodUnitId(unit.getId());
            wholeBlood.setComponentType(ComponentType.WHOLE_BLOOD);
            wholeBlood.setSeparationDate(separationDate);
            wholeBlood.setExpiryDate(unit.getExpiryDate());
            wholeBlood.setStatus(ComponentStatus.AVAILABLE);
            wholeBlood.setVolumeMl(unit.getVolumeMl());
            wholeBlood.setStorageType("Refrigerator (2-6°C)");
            wholeBlood.setStorageTemperature(4.0);
            components.add(wholeBlood);
        }
        
        // Save all components
        componentRepository.saveAll(components);
        
        // Update blood unit status to STORED
        unit.setStatus(BloodUnitStatus.STORED);
        bloodUnitRepository.save(unit);
        
        // Log the component separation
        String componentNames = components.stream()
            .map(c -> c.getComponentType().toString())
            .reduce((a, b) -> a + ", " + b)
            .orElse("NONE");

        trackingService.logAction(
            unit.getBloodUnitId(),
            "COMPONENTS_CREATED",
            performedBy,
            "ROLE_BLOODBANK",
            String.format("Units created based on donation type (%s): %s", donationType, componentNames)
        );
        
        return components;
    }

    /**
     * Find available components for hospital requests.
     * Filters by component type, blood group, and availability.
     */
    public List<BloodComponent> findAvailableComponents(ComponentType componentType, 
                                                       String bloodGroup, 
                                                       int quantity) {
        LocalDate today = LocalDate.now();
        List<BloodComponent> available = componentRepository.findAvailableComponents(
            componentType, bloodGroup, today
        );
        
        // Return only the requested quantity (or less if not enough available)
        return available.subList(0, Math.min(quantity, available.size()));
    }

    /**
     * Get inventory count for a specific component type and blood group.
     */
    public long getAvailableCount(ComponentType componentType, String bloodGroup) {
        return componentRepository.countAvailableComponents(componentType, bloodGroup, LocalDate.now());
    }

    /**
     * Mark expired components.
     * Should be called by scheduled task daily.
     */
    @Transactional
    public void markExpiredComponents() {
        LocalDate today = LocalDate.now();
        List<BloodComponent> expiredComponents = componentRepository
            .findByExpiryDateBeforeAndStatusNot(today, ComponentStatus.EXPIRED);
        
        for (BloodComponent component : expiredComponents) {
            ComponentStatus previousStatus = component.getStatus();
            component.setStatus(ComponentStatus.EXPIRED);
            componentRepository.save(component);
            
            // Get the blood unit for logging
            BloodUnit unit = bloodUnitRepository.findById(component.getBloodUnitId())
                .orElse(null);
            
            if (unit != null) {
                trackingService.logAction(
                    unit.getBloodUnitId(),
                    "COMPONENT_EXPIRED",
                    "SYSTEM",
                    "SYSTEM",
                    String.format("%s component expired (was %s, now EXPIRED). Expiry date: %s",
                        component.getComponentType(), previousStatus, component.getExpiryDate())
                );
            }
        }
    }

    /**
     * Update component status.
     */
    @Transactional
    public void updateComponentStatus(Long componentId, ComponentStatus newStatus, 
                                     String performedBy, String role) {
        BloodComponent component = componentRepository.findById(componentId)
            .orElseThrow(() -> new RuntimeException("Component not found"));
        
        ComponentStatus previousStatus = component.getStatus();
        component.setStatus(newStatus);
        componentRepository.save(component);
        
        // Get the blood unit for logging
        BloodUnit unit = bloodUnitRepository.findById(component.getBloodUnitId())
            .orElse(null);
        
        if (unit != null) {
            trackingService.logAction(
                unit.getBloodUnitId(),
                "COMPONENT_STATUS_CHANGED",
                performedBy,
                role,
                String.format("%s component status changed from %s to %s",
                    component.getComponentType(), previousStatus, newStatus)
            );
        }
    }

    /**
     * Get all components for a blood unit.
     */
    public List<BloodComponent> getComponentsByBloodUnit(Long bloodUnitId) {
        return componentRepository.findByBloodUnitId(bloodUnitId);
    }

    /**
     * Find component by component ID.
     */
    public BloodComponent findByComponentId(String componentId) {
        return componentRepository.findByComponentId(componentId)
            .orElseThrow(() -> new RuntimeException("Component not found: " + componentId));
    }
}
