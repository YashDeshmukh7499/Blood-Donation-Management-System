package com.example.bloodchain.controller;

import com.example.bloodchain.dto.AppointmentData;
import com.example.bloodchain.dto.CompletionData;
import com.example.bloodchain.dto.RejectionData;
import com.example.bloodchain.model.BloodUnit;
import com.example.bloodchain.model.DonationRequest;
import com.example.bloodchain.model.DonationRequestStatus;
import com.example.bloodchain.repository.DonationRequestRepository;
import com.example.bloodchain.service.DonationCompletionService;
import com.example.bloodchain.service.EligibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for managing donation requests.
 * Handles the complete lifecycle: Request -> Approval -> Scheduling -> Completion.
 */
@RestController
@RequestMapping("/api/donation-requests")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class DonationRequestController {

    @Autowired
    private DonationRequestRepository donationRequestRepo;

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private DonationCompletionService completionService;

    @Autowired
    private com.example.bloodchain.repository.DonorRepository donorRepo;

    @Autowired
    private com.example.bloodchain.repository.UserRepository userRepo;

    /**
     * Submit a new donation request.
     * Enforces server-side eligibility checks (Phase 2 Requirement).
     */
    @PostMapping("/request")
    public ResponseEntity<String> requestDonation(@RequestBody DonationRequest request) {
        // 0. Resolve Donor ID (Create if not exists)
        if (request.getDonorId() == null) {
            com.example.bloodchain.model.Donor donor = donorRepo.findByEmail(request.getDonorEmail());
            if (donor == null) {
                // Create new donor from User details
                com.example.bloodchain.model.User user = userRepo.findById(request.getDonorEmail()).orElse(null);
                donor = new com.example.bloodchain.model.Donor();
                donor.setEmail(request.getDonorEmail());
                if (user != null) {
                    donor.setName(user.getName());
                    donor.setBloodGroup(user.getBloodGroup());
                    donor.setCity(user.getCity());
                } else {
                    donor.setName("Unknown");
                }
                donorRepo.save(donor);
            }
            request.setDonorId(donor.getDonorId());
        }

        // 1. Server-side Eligibility Check
        if (!eligibilityService.isEligible(request.getDonorEmail())) {
            String reason = eligibilityService.getIneligibilityReason(request.getDonorEmail());
            return ResponseEntity.badRequest().body("❌ Not Eligible: " + reason);
        }

        // 2. Set initial status
        request.setStatus(DonationRequestStatus.PENDING);
        request.setCreatedAt(LocalDateTime.now());
        
        donationRequestRepo.save(request);
        return ResponseEntity.ok("✅ Donation request submitted successfully! Waiting for approval.");
    }

    /**
     * Get all requests for a specific donor.
     */
    @GetMapping("/requests/donor/{email}")
    public List<DonationRequest> getDonorRequests(@PathVariable String email) {
        return donationRequestRepo.findByDonorEmailOrderByRequestDateDesc(email);
    }

    /**
     * Get all requests for a specific blood bank.
     */
    @GetMapping("/requests/bloodbank/{bankId}")
    public List<DonationRequest> getBloodBankRequests(@PathVariable Integer bankId) {
        return donationRequestRepo.findByBloodBankIdOrderByRequestDateDesc(bankId);
    }
    
    /**
     * Get pending requests for a blood bank.
     */
    @GetMapping("/requests/bloodbank/{bankId}/pending")
    public List<DonationRequest> getPendingBloodBankRequests(@PathVariable Integer bankId) {
        return donationRequestRepo.findByBloodBankIdAndStatusOrderByRequestDateDesc(
            bankId, DonationRequestStatus.PENDING);
    }

    /**
     * Approve a donation request.
     * Transitions status: PENDING -> APPROVED
     */
    @PutMapping("/requests/{id}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        return updateRequestStatus(id, DonationRequestStatus.APPROVED, "Request approved");
    }

    /**
     * Reject a donation request.
     * Transitions status: PENDING -> REJECTED
     */
    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id, @RequestBody RejectionData data) {
        DonationRequest request = donationRequestRepo.findById(id)
            .orElse(null);
        if (request == null) return ResponseEntity.notFound().build();

        request.setStatus(DonationRequestStatus.REJECTED);
        request.setRejectionReason(data.getReason());
        request.setUpdatedAt(LocalDateTime.now());
        donationRequestRepo.save(request);

        return ResponseEntity.ok("Request rejected");
    }

    /**
     * Schedule an appointment for an approved request.
     * Transitions status: APPROVED -> SCHEDULED
     */
    @PutMapping("/requests/{id}/schedule")
    public ResponseEntity<String> scheduleAppointment(@PathVariable Long id, @RequestBody AppointmentData data) {
        DonationRequest request = donationRequestRepo.findById(id)
            .orElse(null);
        if (request == null) return ResponseEntity.notFound().build();

        // Only APPROVED requests can be scheduled
        if (request.getStatus() != DonationRequestStatus.APPROVED && 
            request.getStatus() != DonationRequestStatus.SCHEDULED) { // Allow rescheduling
            return ResponseEntity.badRequest().body("Request must be APPROVED to schedule");
        }

        request.setStatus(DonationRequestStatus.SCHEDULED);
        request.setAppointmentDate(data.getAppointmentDate());
        request.setAppointmentTime(data.getAppointmentTime());
        request.setAppointmentNotes(data.getNotes());
        request.setUpdatedAt(LocalDateTime.now());
        donationRequestRepo.save(request);

        return ResponseEntity.ok("Appointment scheduled successfully");
    }

    /**
     * Complete a donation request (Donor donated blood).
     * Transitions status: SCHEDULED -> COMPLETED
     * Creates BloodUnit and logs to Blockchain.
     */
    @PutMapping("/requests/{id}/complete")
    public ResponseEntity<?> completeDonation(@PathVariable Long id, @RequestBody CompletionData data) {
        try {
            BloodUnit unit = completionService.completeDonation(id, data);
            return ResponseEntity.ok(unit);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to complete donation: " + e.getMessage());
        }
    }

    /**
     * Cancel a donation request (Soft delete).
     * Transitions status: PENDING/APPROVED/SCHEDULED -> CANCELLED
     */
    @PutMapping("/requests/{id}/cancel")
    public ResponseEntity<String> cancelRequest(@PathVariable Long id) {
        DonationRequest request = donationRequestRepo.findById(id)
            .orElse(null);
        if (request == null) return ResponseEntity.notFound().build();

        // Only allow cancellation if not already final
        if (request.getStatus() == DonationRequestStatus.COMPLETED || 
            request.getStatus() == DonationRequestStatus.REJECTED) {
            return ResponseEntity.badRequest().body("Cannot cancel finalized request");
        }

        request.setStatus(DonationRequestStatus.CANCELLED);
        request.setUpdatedAt(LocalDateTime.now());
        donationRequestRepo.save(request);

        return ResponseEntity.ok("Request cancelled successfully");
    }

    // Helper method for simple status updates
    private ResponseEntity<String> updateRequestStatus(Long id, DonationRequestStatus status, String successMessage) {
        DonationRequest request = donationRequestRepo.findById(id)
            .orElse(null);
        if (request == null) return ResponseEntity.notFound().build();

        request.setStatus(status);
        request.setUpdatedAt(LocalDateTime.now());
        donationRequestRepo.save(request);

        return ResponseEntity.ok(successMessage);
    }
}
