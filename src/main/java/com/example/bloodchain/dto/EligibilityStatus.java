package com.example.bloodchain.dto;

import java.time.LocalDate;

/**
 * DTO for eligibility status response.
 */
public class EligibilityStatus {
    private boolean eligible;
    private String reason;
    private LocalDate nextEligibleDate;

    // Constructors
    public EligibilityStatus() {}

    public EligibilityStatus(boolean eligible, String reason, LocalDate nextEligibleDate) {
        this.eligible = eligible;
        this.reason = reason;
        this.nextEligibleDate = nextEligibleDate;
    }

    // Getters and Setters
    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }
}
