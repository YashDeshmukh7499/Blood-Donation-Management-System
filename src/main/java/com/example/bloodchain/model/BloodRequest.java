package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing hospital blood requests.
 * Implements BloodunitRequested() and OrderApproval() from IEEE paper.
 */
@Entity
@Table(name = "blood_requests")
public class BloodRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_number", unique = true, nullable = false, length = 50)
    private String requestNumber;  // e.g., "REQ-2025-000001"

    @Column(name = "hospital_id", nullable = false)
    private Integer hospitalId;

    @Column(name = "hospital_email", nullable = false)
    private String hospitalEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private ComponentType componentType;

    @Column(name = "blood_group", nullable = false, length = 5)
    private String bloodGroup;

    @Column(nullable = false)
    private Integer quantity;  // Number of units requested

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestUrgency urgency = RequestUrgency.ROUTINE;

    @Column(name = "patient_name", length = 255)
    private String patientName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(columnDefinition = "TEXT")
    private String reason;  // Reason for blood request

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();

    @Column(name = "required_by_date")
    private LocalDate requiredByDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BloodRequestStatus status = BloodRequestStatus.REQUESTED;

    @Column(name = "approved_by")
    private String approvedBy;  // Blood bank user email

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "approved_quantity")
    private Integer approvedQuantity;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public BloodRequest() {}

    public BloodRequest(String requestNumber, Integer hospitalId, String hospitalEmail,
                       ComponentType componentType, String bloodGroup, Integer quantity) {
        this.requestNumber = requestNumber;
        this.hospitalId = hospitalId;
        this.hospitalEmail = hospitalEmail;
        this.componentType = componentType;
        this.bloodGroup = bloodGroup;
        this.quantity = quantity;
    }

    // Lifecycle callback
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(String requestNumber) {
        this.requestNumber = requestNumber;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
    }

    public String getHospitalEmail() {
        return hospitalEmail;
    }

    public void setHospitalEmail(String hospitalEmail) {
        this.hospitalEmail = hospitalEmail;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public RequestUrgency getUrgency() {
        return urgency;
    }

    public void setUrgency(RequestUrgency urgency) {
        this.urgency = urgency;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getRequiredByDate() {
        return requiredByDate;
    }

    public void setRequiredByDate(LocalDate requiredByDate) {
        this.requiredByDate = requiredByDate;
    }

    public BloodRequestStatus getStatus() {
        return status;
    }

    public void setStatus(BloodRequestStatus status) {
        this.status = status;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public Integer getApprovedQuantity() {
        return approvedQuantity;
    }

    public void setApprovedQuantity(Integer approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
