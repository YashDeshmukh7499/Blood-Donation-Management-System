package com.example.bloodchain.model;

/**
 * Enum representing the testing status of blood units.
 */
public enum TestStatus {
    PENDING,        // Testing not yet completed
    PASSED,         // All tests negative
    FAILED          // One or more tests positive
}
