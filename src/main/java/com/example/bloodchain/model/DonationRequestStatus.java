package com.example.bloodchain.model;

/**
 * Enum representing the status of donation requests from donors.
 * Enhanced workflow: PENDING → APPROVED → SCHEDULED → COMPLETED
 */
public enum DonationRequestStatus {
    PENDING,        // Awaiting blood bank review
    APPROVED,       // Blood bank approved, awaiting appointment scheduling
    SCHEDULED,      // Appointment date/time confirmed
    REJECTED,       // Blood bank rejected (ineligible donor)
    COMPLETED,      // Blood collected successfully
    CANCELLED       // Donor cancelled the request
}
