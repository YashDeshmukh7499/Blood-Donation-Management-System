package com.example.bloodchain.model;

/**
 * Enum representing the status of blood requests from hospitals.
 */
public enum BloodRequestStatus {
    REQUESTED,          // Hospital submitted request
    APPROVED,           // Blood bank approved and assigned components
    PARTIALLY_APPROVED, // Only partial quantity available
    REJECTED,           // Blood bank rejected (insufficient stock)
    DISPATCHED,         // Blood dispatched to hospital
    COMPLETED,          // Hospital received and confirmed
    CANCELLED           // Request cancelled
}
