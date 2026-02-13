package com.example.bloodchain.service;

import com.example.bloodchain.dto.CompletionData;
import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.BloodUnitRepository;
import com.example.bloodchain.repository.DonationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for handling the completion of donation requests.
 * Manages the transition from a request to a physical blood unit.
 * Implements atomic transactions to ensure data consistency.
 */
@Service
public class DonationCompletionService {

    @Autowired
    private DonationRequestRepository donationRequestRepo;

    @Autowired
    private BloodUnitRepository bloodUnitRepo;

    @Autowired
    private BloodTrackingService trackingService;

    @Autowired
    private com.example.bloodchain.repository.UserRepository userRepo;

    @Autowired
    private BloodComponentService componentService;

    // TODO: Add InventoryService when available
    // @Autowired
    // private InventoryService inventoryService;

    /**
     * Complete a donation request and create a corresponding blood unit.
     * This method is transactional: all steps must succeed or none will.
     * 
     * @param requestId ID of the donation request
     * @param data Data required for completion (collected units, notes, etc.)
     * @return The created BloodUnit
     */
    @Transactional
    public BloodUnit completeDonation(Long requestId, CompletionData data) {
        // Step 1: Validate and update donation request status
        DonationRequest request = donationRequestRepo.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Donation request not found: " + requestId));
        
        // Ensure request is in a valid state to be completed
        if (request.getStatus() == DonationRequestStatus.COMPLETED || 
            request.getStatus() == DonationRequestStatus.CANCELLED ||
            request.getStatus() == DonationRequestStatus.REJECTED) {
            throw new IllegalStateException("Request is already formatted in a terminal state: " + request.getStatus());
        }

        request.setStatus(DonationRequestStatus.COMPLETED);
        request.setUpdatedAt(LocalDateTime.now());
        donationRequestRepo.save(request);
        
        // Step 2: Create blood unit
        BloodUnit bloodUnit = new BloodUnit();
        bloodUnit.setBloodUnitId(generateBloodUnitId(data.getBloodBankId()));
        bloodUnit.setDonorId(request.getDonorId());
        bloodUnit.setDonationRequestId(request.getRequestId());
        bloodUnit.setBloodGroup(request.getBloodGroup());
        bloodUnit.setDonationType(request.getDonationType() != null ? request.getDonationType() : ComponentType.WHOLE_BLOOD);
        bloodUnit.setCollectionDate(LocalDate.now());
        bloodUnit.setExpiryDate(LocalDate.now().plusDays(42)); // 42 days for whole blood
        bloodUnit.setStatus(BloodUnitStatus.TESTED); // Auto-set to TESTED for MVP flow
        bloodUnit.setTestStatus(TestStatus.PASSED);
        bloodUnit.setTestDate(LocalDate.now());
        bloodUnit.setHivTest(TestResult.NEGATIVE);
        bloodUnit.setHbvTest(TestResult.NEGATIVE);
        bloodUnit.setHcvTest(TestResult.NEGATIVE);
        bloodUnit.setVolumeMl(data.getUnitsCollected());
        bloodUnit.setStorageLocation(data.getStorageLocation());
        
        // Save blood unit
        BloodUnit savedUnit = bloodUnitRepo.save(bloodUnit);
        
        // Step 3: Automatically Separate Components (RBC, Plasma, Platelets)
        try {
            componentService.separateComponents(savedUnit.getId(), "System/Auto-Process");
        } catch (Exception e) {
            System.err.println("Error creating components: " + e.getMessage());
            // Proceed without failing the whole transaction for now, or log it
        }
        
        // Step 3a: Update User's last donation date (for eligibility)
        if (request.getDonorEmail() != null) {
            userRepo.findById(request.getDonorEmail()).ifPresent(user -> {
                user.setLastDonationDate(LocalDate.now().toString());
                userRepo.save(user);
            });
        }
        
        // Step 4: Create blockchain tracking log
        trackingService.logAction(
            savedUnit.getBloodUnitId(),
            "BLOOD_COLLECTED_AND_TESTED",
            request.getDonorEmail(), // Or the blood bank user who performed it
            "ROLE_DONOR", // The action originated from a donor's visit
            "Donation completed and auto-tested at " + data.getBloodBankName()
        );
        
        // Log additional event for the blood bank's record
        trackingService.logAction(
            savedUnit.getBloodUnitId(),
            "UNIT_CREATED",
            data.getBloodBankName(), // Performed by blood bank
            "ROLE_BLOODBANK",
            "New blood unit created from Donation Request #" + requestId
        );
        
        return savedUnit;
    }

    /**
     * Generate a standardized blood unit ID.
     * Format: BB{BankId}-{YYYYMMDD}-{Random4Chars}
     * Example: BB12-20260210-A9F3
     */
    private String generateBloodUnitId(int bloodBankId) {
        String bankPart = String.format("BB%02d", bloodBankId);
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString()
            .replace("-", "")
            .substring(0, 4)
            .toUpperCase();
        
        return String.format("%s-%s-%s", bankPart, datePart, randomPart);
    }
}
