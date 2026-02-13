package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity mapping blood requests to assigned blood components.
 * Tracks which components are assigned to which hospital requests.
 */
@Entity
@Table(name = "blood_request_components")
public class BloodRequestComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    @Column(name = "blood_component_id", nullable = false)
    private Long bloodComponentId;

    @Column(name = "assigned_date", nullable = false)
    private LocalDateTime assignedDate = LocalDateTime.now();

    @Column(name = "dispatch_date")
    private LocalDateTime dispatchDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public BloodRequestComponent() {}

    public BloodRequestComponent(Long bloodRequestId, Long bloodComponentId) {
        this.bloodRequestId = bloodRequestId;
        this.bloodComponentId = bloodComponentId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBloodRequestId() {
        return bloodRequestId;
    }

    public void setBloodRequestId(Long bloodRequestId) {
        this.bloodRequestId = bloodRequestId;
    }

    public Long getBloodComponentId() {
        return bloodComponentId;
    }

    public void setBloodComponentId(Long bloodComponentId) {
        this.bloodComponentId = bloodComponentId;
    }

    public LocalDateTime getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDateTime assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDateTime getDispatchDate() {
        return dispatchDate;
    }

    public void setDispatchDate(LocalDateTime dispatchDate) {
        this.dispatchDate = dispatchDate;
    }

    public LocalDateTime getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(LocalDateTime receivedDate) {
        this.receivedDate = receivedDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
