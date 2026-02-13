package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing patient blood transfusion records.
 * Implements BloodUnitPrescription() and BloodUnitTransfusion() from IEEE paper.
 */
@Entity
@Table(name = "blood_usage")
public class BloodUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blood_component_id", nullable = false)
    private Long bloodComponentId;

    @Column(name = "blood_request_id", nullable = false)
    private Long bloodRequestId;

    @Column(name = "hospital_id", nullable = false)
    private Integer hospitalId;

    @Column(name = "patient_name", nullable = false, length = 255)
    private String patientName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "patient_blood_group", length = 5)
    private String patientBloodGroup;

    @Column(name = "doctor_name", length = 255)
    private String doctorName;

    @Column(name = "nurse_name", length = 255)
    private String nurseName;

    @Column(name = "transfusion_date", nullable = false)
    private LocalDateTime transfusionDate;

    @Column(name = "transfusion_reason", columnDefinition = "TEXT")
    private String transfusionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "adverse_reaction", length = 20)
    private AdverseReaction adverseReaction = AdverseReaction.NONE;

    @Column(name = "reaction_details", columnDefinition = "TEXT")
    private String reactionDetails;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public BloodUsage() {}

    public BloodUsage(Long bloodComponentId, Long bloodRequestId, Integer hospitalId,
                     String patientName, LocalDateTime transfusionDate) {
        this.bloodComponentId = bloodComponentId;
        this.bloodRequestId = bloodRequestId;
        this.hospitalId = hospitalId;
        this.patientName = patientName;
        this.transfusionDate = transfusionDate;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBloodComponentId() {
        return bloodComponentId;
    }

    public void setBloodComponentId(Long bloodComponentId) {
        this.bloodComponentId = bloodComponentId;
    }

    public Long getBloodRequestId() {
        return bloodRequestId;
    }

    public void setBloodRequestId(Long bloodRequestId) {
        this.bloodRequestId = bloodRequestId;
    }

    public Integer getHospitalId() {
        return hospitalId;
    }

    public void setHospitalId(Integer hospitalId) {
        this.hospitalId = hospitalId;
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

    public String getPatientBloodGroup() {
        return patientBloodGroup;
    }

    public void setPatientBloodGroup(String patientBloodGroup) {
        this.patientBloodGroup = patientBloodGroup;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getNurseName() {
        return nurseName;
    }

    public void setNurseName(String nurseName) {
        this.nurseName = nurseName;
    }

    public LocalDateTime getTransfusionDate() {
        return transfusionDate;
    }

    public void setTransfusionDate(LocalDateTime transfusionDate) {
        this.transfusionDate = transfusionDate;
    }

    public String getTransfusionReason() {
        return transfusionReason;
    }

    public void setTransfusionReason(String transfusionReason) {
        this.transfusionReason = transfusionReason;
    }

    public AdverseReaction getAdverseReaction() {
        return adverseReaction;
    }

    public void setAdverseReaction(AdverseReaction adverseReaction) {
        this.adverseReaction = adverseReaction;
    }

    public String getReactionDetails() {
        return reactionDetails;
    }

    public void setReactionDetails(String reactionDetails) {
        this.reactionDetails = reactionDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
