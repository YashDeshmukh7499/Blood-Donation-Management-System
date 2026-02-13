package com.example.bloodchain.dto;

/**
 * DTO for donation rejection data.
 */
public class RejectionData {
    private String reason;

    // Constructors
    public RejectionData() {}

    public RejectionData(String reason) {
        this.reason = reason;
    }

    // Getters and Setters
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
