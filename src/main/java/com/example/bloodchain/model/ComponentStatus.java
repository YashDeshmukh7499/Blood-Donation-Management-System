package com.example.bloodchain.model;

/**
 * Enum representing the status of individual blood components.
 */
public enum ComponentStatus {
    AVAILABLE,      // Available for hospital requests
    RESERVED,       // Reserved for a specific hospital request
    DISPATCHED,     // In transit to hospital
    RECEIVED,       // Received by hospital (In Stock)
    USED,           // Transfused to patient
    EXPIRED         // Past expiry date
}
