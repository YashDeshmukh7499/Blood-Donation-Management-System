package com.example.bloodchain.service;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.BloodUnitRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.*;

/**
 * Service for managing blood units.
 * Implements state-based workflow and traceability.
 */
@Service
public class BloodUnitService {

    @Autowired
    private BloodUnitRepository bloodUnitRepository;

    @Autowired
    private BloodTrackingService trackingService;

    // State transition validation map
    private static final Map<BloodUnitStatus, List<BloodUnitStatus>> ALLOWED_TRANSITIONS = Map.of(
        BloodUnitStatus.COLLECTED, List.of(BloodUnitStatus.TESTED, BloodUnitStatus.REJECTED),
        BloodUnitStatus.TESTED, List.of(BloodUnitStatus.STORED, BloodUnitStatus.REJECTED),
        BloodUnitStatus.STORED, List.of(BloodUnitStatus.REQUESTED, BloodUnitStatus.EXPIRED),
        BloodUnitStatus.REQUESTED, List.of(BloodUnitStatus.APPROVED, BloodUnitStatus.STORED),
        BloodUnitStatus.APPROVED, List.of(BloodUnitStatus.DISPATCHED),
        BloodUnitStatus.DISPATCHED, List.of(BloodUnitStatus.RECEIVED),
        BloodUnitStatus.RECEIVED, List.of(BloodUnitStatus.USED)
    );

    /**
     * Create a new blood unit after collection.
     */
    @Transactional
    public BloodUnit createBloodUnit(Integer donorId, Long donationRequestId, String bloodGroup,
                                    Integer volumeMl, String storageLocation, String performedBy) {
        // Generate unique blood unit ID
        String bloodUnitId = generateUniqueBloodUnitId();
        
        // Create blood unit
        BloodUnit unit = new BloodUnit();
        unit.setBloodUnitId(bloodUnitId);
        unit.setDonorId(donorId);
        unit.setDonationRequestId(donationRequestId);
        unit.setBloodGroup(bloodGroup);
        unit.setCollectionDate(LocalDate.now());
        unit.setExpiryDate(LocalDate.now().plusDays(35));  // 35 days for whole blood
        unit.setStatus(BloodUnitStatus.COLLECTED);
        unit.setVolumeMl(volumeMl != null ? volumeMl : 450);
        unit.setStorageLocation(storageLocation);
        
        // Generate hash for verification
        unit.setBlockHash(generateBloodUnitHash(unit));
        
        // Save blood unit
        BloodUnit savedUnit = bloodUnitRepository.save(unit);
        
        // Log the action
        trackingService.logAction(
            bloodUnitId,
            "BLOOD_COLLECTED",
            performedBy,
            "ROLE_BLOODBANK",
            String.format("Blood collected from donor ID: %d, Volume: %dml", donorId, unit.getVolumeMl())
        );
        
        return savedUnit;
    }

    /**
     * Update blood unit status with validation.
     * Ensures only valid state transitions are allowed.
     */
    @Transactional
    public void updateStatus(Long unitId, BloodUnitStatus newStatus, String performedBy, String role) {
        BloodUnit unit = bloodUnitRepository.findById(unitId)
            .orElseThrow(() -> new RuntimeException("Blood unit not found"));
        
        BloodUnitStatus currentStatus = unit.getStatus();
        
        // Validate state transition
        if (!isValidTransition(currentStatus, newStatus)) {
            throw new IllegalStateException(
                String.format("Invalid state transition from %s to %s", currentStatus, newStatus)
            );
        }
        
        // Update status
        unit.setStatus(newStatus);
        bloodUnitRepository.save(unit);
        
        // Log the status change
        trackingService.logStatusChange(
            unit.getBloodUnitId(),
            "STATUS_CHANGED",
            performedBy,
            role,
            currentStatus.toString(),
            newStatus.toString(),
            String.format("Status changed from %s to %s", currentStatus, newStatus)
        );
    }

    /**
     * Record blood test results.
     */
    @Transactional
    public void recordTestResults(Long unitId, TestResult hivTest, TestResult hbvTest, 
                                  TestResult hcvTest, String performedBy) {
        BloodUnit unit = bloodUnitRepository.findById(unitId)
            .orElseThrow(() -> new RuntimeException("Blood unit not found"));
        
        if (unit.getStatus() != BloodUnitStatus.COLLECTED) {
            throw new IllegalStateException("Blood must be in COLLECTED status to test");
        }
        
        // Record test results
        unit.setTestDate(LocalDate.now());
        unit.setHivTest(hivTest);
        unit.setHbvTest(hbvTest);
        unit.setHcvTest(hcvTest);
        
        // Determine test status
        boolean allNegative = hivTest == TestResult.NEGATIVE && 
                             hbvTest == TestResult.NEGATIVE && 
                             hcvTest == TestResult.NEGATIVE;
        
        if (allNegative) {
            unit.setTestStatus(TestStatus.PASSED);
            unit.setStatus(BloodUnitStatus.TESTED);
            
            trackingService.logStatusChange(
                unit.getBloodUnitId(),
                "BLOOD_TESTED",
                performedBy,
                "ROLE_BLOODBANK",
                BloodUnitStatus.COLLECTED.toString(),
                BloodUnitStatus.TESTED.toString(),
                "All tests passed (HIV: NEGATIVE, HBV: NEGATIVE, HCV: NEGATIVE)"
            );
        } else {
            unit.setTestStatus(TestStatus.FAILED);
            unit.setStatus(BloodUnitStatus.REJECTED);
            
            List<String> failedTests = new ArrayList<>();
            if (hivTest == TestResult.POSITIVE) failedTests.add("HIV");
            if (hbvTest == TestResult.POSITIVE) failedTests.add("HBV");
            if (hcvTest == TestResult.POSITIVE) failedTests.add("HCV");
            
            trackingService.logStatusChange(
                unit.getBloodUnitId(),
                "BLOOD_REJECTED",
                performedBy,
                "ROLE_BLOODBANK",
                BloodUnitStatus.COLLECTED.toString(),
                BloodUnitStatus.REJECTED.toString(),
                "Failed tests: " + String.join(", ", failedTests)
            );
        }
        
        bloodUnitRepository.save(unit);
    }

    /**
     * Find blood unit by blood unit ID.
     */
    public BloodUnit findByBloodUnitId(String bloodUnitId) {
        return bloodUnitRepository.findByBloodUnitId(bloodUnitId)
            .orElseThrow(() -> new RuntimeException("Blood unit not found: " + bloodUnitId));
    }

    /**
     * Find all blood units by donor ID.
     */
    public List<BloodUnit> findByDonorId(Integer donorId) {
        return bloodUnitRepository.findByDonorId(donorId);
    }

    /**
     * Find available blood units by blood group.
     */
    public List<BloodUnit> findAvailableByBloodGroup(String bloodGroup) {
        return bloodUnitRepository.findByBloodGroupAndStatus(bloodGroup, BloodUnitStatus.STORED);
    }

    /**
     * Mark expired blood units.
     * Should be called by scheduled task daily.
     */
    @Transactional
    public void markExpiredBloodUnits() {
        LocalDate today = LocalDate.now();
        List<BloodUnit> expiredUnits = bloodUnitRepository
            .findByExpiryDateBeforeAndStatusNot(today, BloodUnitStatus.EXPIRED);
        
        for (BloodUnit unit : expiredUnits) {
            BloodUnitStatus previousStatus = unit.getStatus();
            unit.setStatus(BloodUnitStatus.EXPIRED);
            bloodUnitRepository.save(unit);
            
            trackingService.logStatusChange(
                unit.getBloodUnitId(),
                "BLOOD_EXPIRED",
                "SYSTEM",
                "SYSTEM",
                previousStatus.toString(),
                BloodUnitStatus.EXPIRED.toString(),
                "Blood unit expired on " + unit.getExpiryDate()
            );
        }
    }

    /**
     * Validate state transition.
     */
    private boolean isValidTransition(BloodUnitStatus current, BloodUnitStatus next) {
        List<BloodUnitStatus> allowedNext = ALLOWED_TRANSITIONS.get(current);
        return allowedNext != null && allowedNext.contains(next);
    }

    /**
     * Generate unique blood unit ID.
     * Format: BU-YYYY-NNNNNN
     */
    private String generateUniqueBloodUnitId() {
        int year = Year.now().getValue();
        long count = bloodUnitRepository.count() + 1;
        String bloodUnitId;
        
        do {
            bloodUnitId = String.format("BU-%d-%06d", year, count++);
        } while (bloodUnitRepository.existsByBloodUnitId(bloodUnitId));
        
        return bloodUnitId;
    }

    /**
     * Generate SHA-256 hash for blood unit verification.
     */
    private String generateBloodUnitHash(BloodUnit unit) {
        String data = unit.getBloodUnitId() + 
                     unit.getDonorId() + 
                     unit.getCollectionDate() + 
                     unit.getBloodGroup() +
                     unit.getVolumeMl();
        return DigestUtils.sha256Hex(data);
    }

    /**
     * Verify blood unit hash.
     */
    public boolean verifyBloodUnit(BloodUnit unit) {
        String expectedHash = generateBloodUnitHash(unit);
        return expectedHash.equals(unit.getBlockHash());
    }
}
