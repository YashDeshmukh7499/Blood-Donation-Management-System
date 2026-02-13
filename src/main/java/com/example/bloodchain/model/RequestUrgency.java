package com.example.bloodchain.model;

/**
 * Enum representing the urgency level of hospital blood requests.
 */
public enum RequestUrgency {
    ROUTINE,        // Normal request, no rush
    URGENT,         // Needed soon (within 24 hours)
    EMERGENCY       // Critical, needed immediately
}
