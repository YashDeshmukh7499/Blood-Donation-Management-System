package com.example.bloodchain.model;

/**
 * Enum representing blood component types after separation.
 * Each component has different expiry dates and storage requirements.
 */
public enum ComponentType {
    RBC,            // Red Blood Cells - 42 days shelf life, 2-6°C
    PLASMA,         // Plasma - 1 year shelf life, -18°C
    PLATELETS,      // Platelets - 5 days shelf life, 20-24°C
    WHOLE_BLOOD     // Whole blood - 35 days shelf life
}
