package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing donor appointment requests before physical donation.
 * Implements the CollectWholeBloodUnit initiation from IEEE paper.
 */
@Entity
@Table(name = "donation_requests")
public class DonationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long requestId;

    @Column(name = "donor_id", nullable = false)
    private Integer donorId;

    @Column(name = "blood_bank_id")
    private Integer bloodBankId; // Selected blood bank for donation

    @Column(name = "donor_email", nullable = false)
    private String donorEmail;

    @Column(name = "blood_group", nullable = false, length = 5)
    private String bloodGroup;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate = LocalDate.now();

    @com.fasterxml.jackson.annotation.JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DonationRequestStatus status = DonationRequestStatus.PENDING;

    @Column(name = "health_declaration", columnDefinition = "TEXT")
    private String healthDeclaration;

    @Column(name = "last_donation_date")
    private LocalDate lastDonationDate;

    @Column(name = "age")
    private Integer age;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "approved_by")
    private String approvedBy;  // Blood bank user email

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", length = 20)
    private ComponentType donationType = ComponentType.WHOLE_BLOOD;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Appointment scheduling fields (Phase 2 enhancement)
    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "appointment_time")
    private java.time.LocalTime appointmentTime;

    @Column(name = "appointment_notes", columnDefinition = "TEXT")
    private String appointmentNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public DonationRequest() {}

    public DonationRequest(Integer donorId, String donorEmail, String bloodGroup, 
                          LocalDate preferredDate, String location, ComponentType donationType) {
        this.donorId = donorId;
        this.donorEmail = donorEmail;
        this.bloodGroup = bloodGroup;
        this.preferredDate = preferredDate;
        this.location = location;
        this.donationType = donationType;
    }

    // Lifecycle callback
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public Integer getDonorId() {
        return donorId;
    }

    public void setDonorId(Integer donorId) {
        this.donorId = donorId;
    }

    public Integer getBloodBankId() {
        return bloodBankId;
    }

    public void setBloodBankId(Integer bloodBankId) {
        this.bloodBankId = bloodBankId;
    }

    public String getDonorEmail() {
        return donorEmail;
    }

    public void setDonorEmail(String donorEmail) {
        this.donorEmail = donorEmail;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public DonationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(DonationRequestStatus status) {
        this.status = status;
    }

    public String getHealthDeclaration() {
        return healthDeclaration;
    }

    public void setHealthDeclaration(String healthDeclaration) {
        this.healthDeclaration = healthDeclaration;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
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

    // Getters and Setters for appointment fields
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public java.time.LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(java.time.LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getAppointmentNotes() {
        return appointmentNotes;
    }

    public void setAppointmentNotes(String appointmentNotes) {
        this.appointmentNotes = appointmentNotes;
    }

    public ComponentType getDonationType() {
        return donationType;
    }

    public void setDonationType(ComponentType donationType) {
        this.donationType = donationType;
    }
}
