package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * CRITICAL: Immutable audit trail entity.
 * This table is APPEND-ONLY - no UPDATE or DELETE operations allowed.
 * Simulates blockchain event logs for complete traceability.
 * Database triggers should prevent modification and deletion.
 */
@Entity
@Table(name = "blood_tracking_log")
public class BloodTrackingLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_unit_id", nullable = false, length = 50)
    private String bloodUnitId;  // References blood_units.blood_unit_id

    @Column(nullable = false, length = 100)
    private String action;  // e.g., "BLOOD_COLLECTED", "BLOOD_TESTED", "BLOOD_DISPATCHED"

    @Column(name = "performed_by", nullable = false)
    private String performedBy;  // User email who performed the action

    @Column(name = "performed_by_role", nullable = false, length = 20)
    private String performedByRole;  // ROLE_DONOR, ROLE_BLOODBANK, ROLE_HOSPITAL, ROLE_ADMIN, SYSTEM

    @Column(name = "previous_status", length = 50)
    private String previousStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(columnDefinition = "TEXT")
    private String details;  // Additional context or JSON data

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "previous_hash", length = 64)
    private String previousHash;

    @Column(name = "hash", length = 64)
    private String hash;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;  // Optional: track IP for security

    // Constructors
    public BloodTrackingLog() {}

    public BloodTrackingLog(String bloodUnitId, String action, String performedBy, 
                           String performedByRole, String details) {
        this.bloodUnitId = bloodUnitId;
        this.action = action;
        this.performedBy = performedBy;
        this.performedByRole = performedByRole;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    // Getters only - no setters for immutability (except for initial creation)
    public Long getId() {
        return id;
    }

    public String getBloodUnitId() {
        return bloodUnitId;
    }

    public void setBloodUnitId(String bloodUnitId) {
        this.bloodUnitId = bloodUnitId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getPerformedByRole() {
        return performedByRole;
    }

    public void setPerformedByRole(String performedByRole) {
        this.performedByRole = performedByRole;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
}
