package com.example.bloodchain.service;

import com.example.bloodchain.model.DonationRequest;
import com.example.bloodchain.model.DonationRequestStatus;
import com.example.bloodchain.repository.DonationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing donation requests from donors.
 */
@Service
public class DonationRequestService {

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    /**
     * Create a new donation request.
     */
    @Transactional
    public DonationRequest createDonationRequest(DonationRequest request) {
        // Validate donor eligibility
        validateDonorEligibility(request);
        
        request.setRequestDate(LocalDate.now());
        request.setStatus(DonationRequestStatus.PENDING);
        
        return donationRequestRepository.save(request);
    }

    /**
     * Approve a donation request.
     */
    @Transactional
    public void approveDonationRequest(Long requestId, String approvedBy) {
        DonationRequest request = donationRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Donation request not found"));
        
        if (request.getStatus() != DonationRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be approved");
        }
        
        request.setStatus(DonationRequestStatus.APPROVED);
        request.setApprovedBy(approvedBy);
        request.setApprovedDate(LocalDateTime.now());
        
        donationRequestRepository.save(request);
    }

    /**
     * Reject a donation request.
     */
    @Transactional
    public void rejectDonationRequest(Long requestId, String rejectionReason, String approvedBy) {
        DonationRequest request = donationRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Donation request not found"));
        
        if (request.getStatus() != DonationRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }
        
        request.setStatus(DonationRequestStatus.REJECTED);
        request.setRejectionReason(rejectionReason);
        request.setApprovedBy(approvedBy);
        request.setApprovedDate(LocalDateTime.now());
        
        donationRequestRepository.save(request);
    }

    /**
     * Mark request as completed after blood collection.
     */
    @Transactional
    public void markAsCompleted(Long requestId) {
        DonationRequest request = donationRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Donation request not found"));
        
        request.setStatus(DonationRequestStatus.COMPLETED);
        donationRequestRepository.save(request);
    }

    /**
     * Find all pending donation requests.
     */
    public List<DonationRequest> findPendingRequests() {
        return donationRequestRepository.findByStatusOrderByRequestDateAsc(DonationRequestStatus.PENDING);
    }

    /**
     * Find all donation requests by donor email.
     */
    public List<DonationRequest> findByDonorEmail(String donorEmail) {
        return donationRequestRepository.findByDonorEmailOrderByRequestDateDesc(donorEmail);
    }

    /**
     * Validate donor eligibility.
     */
    private void validateDonorEligibility(DonationRequest request) {
        // Age validation (18-65 years)
        if (request.getAge() != null && (request.getAge() < 18 || request.getAge() > 65)) {
            throw new IllegalArgumentException("Donor must be between 18 and 65 years old");
        }
        
        // Weight validation (>50 kg)
        if (request.getWeight() != null && request.getWeight() < 50) {
            throw new IllegalArgumentException("Donor must weigh at least 50 kg");
        }
        
        // Last donation date validation (>90 days ago)
        if (request.getLastDonationDate() != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(
                request.getLastDonationDate(), 
                LocalDate.now()
            );
            
            if (daysSinceLastDonation < 90) {
                throw new IllegalArgumentException(
                    String.format("Must wait 90 days between donations. Last donation was %d days ago", 
                    daysSinceLastDonation)
                );
            }
        }
    }
}
