package com.example.bloodchain.model;

/**
 * Enum representing the lifecycle status of a blood unit.
 * Implements state-based workflow management as per IEEE paper requirements.
 */
public enum BloodUnitStatus {
    COLLECTED,      // Initial state after donation
    TESTED,         // After lab testing (HIV, HBV, HCV)
    STORED,         // Available in inventory after component separation
    REQUESTED,      // Hospital has requested this blood
    APPROVED,       // Blood bank approved the request
    DISPATCHED,     // In transit to hospital
    RECEIVED,       // Hospital received the blood
    USED,           // Transfused to patient (final state)
    EXPIRED,        // Past expiry date
    REJECTED        // Failed testing or other rejection
}
