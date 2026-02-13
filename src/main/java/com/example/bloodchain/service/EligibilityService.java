package com.example.bloodchain.service;

import com.example.bloodchain.model.Donation;
import com.example.bloodchain.model.DonationRequest;
import com.example.bloodchain.model.User;
import com.example.bloodchain.repository.DonationRepository;
import com.example.bloodchain.repository.DonationRequestRepository;
import com.example.bloodchain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for checking donor eligibility before accepting donation requests.
 * Implements industry-standard eligibility rules for blood donation.
 */
@Service
public class EligibilityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private DonationRequestRepository donationRequestRepository;

    /**
     * Check if a donor is eligible to donate blood.
     * 
     * @param donorEmail Email of the donor
     * @return true if eligible, false otherwise
     */
    public boolean isEligible(String donorEmail) {
        User user = userRepository.findById(donorEmail).orElse(null);
        if (user == null) {
            return false;
        }

        // Rule 1: Age between 18-65
        if (user.getAge() != null && (user.getAge() < 18 || user.getAge() > 65)) {
            return false;
        }

        // Rule 2: Last donation >= 90 days ago
        LocalDate lastDonation = getLastDonationDate(donorEmail);
        if (lastDonation != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(lastDonation, LocalDate.now());
            if (daysSinceLastDonation < 90) {
                return false;
            }
        }

        // Rule 3: No active requests (PENDING/APPROVED/SCHEDULED)
        if (hasActiveRequest(donorEmail)) {
            return false;
        }

        // Rule 4: Hemoglobin check (if available)
        // TODO: Add when medical data is integrated

        return true;
    }

    /**
     * Get detailed reason for ineligibility.
     * 
     * @param donorEmail Email of the donor
     * @return Reason string explaining why donor is not eligible
     */
    public String getIneligibilityReason(String donorEmail) {
        User user = userRepository.findById(donorEmail).orElse(null);
        if (user == null) {
            return "User not found";
        }

        // Check age
        if (user.getAge() != null && user.getAge() < 18) {
            return "You must be at least 18 years old to donate blood";
        }
        if (user.getAge() != null && user.getAge() > 65) {
            return "Blood donation is not recommended for donors over 65 years old";
        }

        // Check last donation date
        LocalDate lastDonation = getLastDonationDate(donorEmail);
        if (lastDonation != null) {
            long daysSinceLastDonation = ChronoUnit.DAYS.between(lastDonation, LocalDate.now());
            if (daysSinceLastDonation < 90) {
                long daysRemaining = 90 - daysSinceLastDonation;
                LocalDate nextEligibleDate = lastDonation.plusDays(90);
                return String.format("You must wait %d more days. You can donate again on %s", 
                    daysRemaining, nextEligibleDate);
            }
        }

        // Check active requests
        if (hasActiveRequest(donorEmail)) {
            return "You already have an active donation request. Please wait for it to be completed or cancelled";
        }

        return "Eligible to donate";
    }

    /**
     * Get the next eligible donation date for a donor.
     * 
     * @param donorEmail Email of the donor
     * @return Next eligible date, or null if eligible now
     */
    public LocalDate getNextEligibleDate(String donorEmail) {
        LocalDate lastDonation = getLastDonationDate(donorEmail);
        if (lastDonation == null) {
            return null; // Never donated, eligible now
        }

        long daysSinceLastDonation = ChronoUnit.DAYS.between(lastDonation, LocalDate.now());
        if (daysSinceLastDonation >= 90) {
            return null; // Already eligible
        }

        return lastDonation.plusDays(90);
    }

    /**
     * Get the last donation date for a donor.
     * 
     * @param donorEmail Email of the donor
     * @return Last donation date, or null if never donated
     */
    private LocalDate getLastDonationDate(String donorEmail) {
        List<Donation> donations = donationRepository.findByEmailOrderByDonationDateDesc(donorEmail);
        if (donations.isEmpty()) {
            return null;
        }
        return donations.get(0).getDonationDate();
    }

    /**
     * Check if donor has any active donation requests.
     * 
     * @param donorEmail Email of the donor
     * @return true if has active request, false otherwise
     */
    private boolean hasActiveRequest(String donorEmail) {
        long count = donationRequestRepository.countActiveRequests(donorEmail);
        return count > 0;
    }

    /**
     * Get donor age from user record.
     * 
     * @param donorEmail Email of the donor
     * @return Age in years, or 0 if not available
     */
    private int getDonorAge(String donorEmail) {
        User user = userRepository.findById(donorEmail).orElse(null);
        return (user != null && user.getAge() != null) ? user.getAge() : 0;
    }
}
