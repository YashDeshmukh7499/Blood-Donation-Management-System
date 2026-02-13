package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core traceability entity representing a single blood donation unit.
 * This is the blockchain address equivalent in the IEEE paper.
 * Each blood unit has a unique ID that tracks it from donor to patient.
 */
@Entity
@Table(name = "blood_units")
public class BloodUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String bloodUnitId;  // e.g., "BU-2025-000001" (blockchain address equivalent)

    @Column(nullable = false)
    private Integer donorId;  // Foreign key to donors table

    @Column(name = "donation_request_id")
    private Long donationRequestId;  // Optional link to donation request

    @Column(nullable = false, length = 5)
    private String bloodGroup;  // A+, B+, O+, AB+, A-, B-, O-, AB-

    @Column(nullable = false)
    private LocalDate collectionDate;

    @Column(nullable = false)
    private LocalDate expiryDate;  // Calculated as collectionDate + 35 days for whole blood

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BloodUnitStatus status = BloodUnitStatus.COLLECTED;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TestStatus testStatus = TestStatus.PENDING;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "hiv_test", length = 10)
    private TestResult hivTest = TestResult.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "hbv_test", length = 10)
    private TestResult hbvTest = TestResult.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "hcv_test", length = 10)
    private TestResult hcvTest = TestResult.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "donation_type", length = 20)
    private ComponentType donationType = ComponentType.WHOLE_BLOOD;

    @Column(name = "volume_ml")
    private Integer volumeMl = 450;  // Standard donation volume in milliliters

    @Column(name = "storage_location", length = 100)
    private String storageLocation;

    @Column(name = "storage_temperature")
    private Double storageTemperature;

    @Column(name = "block_hash", length = 64)
    private String blockHash;  // Optional SHA-256 hash for verification

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public BloodUnit() {}

    public BloodUnit(String bloodUnitId, Integer donorId, String bloodGroup, 
                     LocalDate collectionDate, LocalDate expiryDate, ComponentType donationType) {
        this.bloodUnitId = bloodUnitId;
        this.donorId = donorId;
        this.bloodGroup = bloodGroup;
        this.collectionDate = collectionDate;
        this.expiryDate = expiryDate;
        this.donationType = donationType;
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

    public String getBloodUnitId() {
        return bloodUnitId;
    }

    public void setBloodUnitId(String bloodUnitId) {
        this.bloodUnitId = bloodUnitId;
    }

    public Integer getDonorId() {
        return donorId;
    }

    public void setDonorId(Integer donorId) {
        this.donorId = donorId;
    }

    public Long getDonationRequestId() {
        return donationRequestId;
    }

    public void setDonationRequestId(Long donationRequestId) {
        this.donationRequestId = donationRequestId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public LocalDate getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(LocalDate collectionDate) {
        this.collectionDate = collectionDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public BloodUnitStatus getStatus() {
        return status;
    }

    public void setStatus(BloodUnitStatus status) {
        this.status = status;
    }

    public TestStatus getTestStatus() {
        return testStatus;
    }

    public void setTestStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public LocalDate getTestDate() {
        return testDate;
    }

    public void setTestDate(LocalDate testDate) {
        this.testDate = testDate;
    }

    public TestResult getHivTest() {
        return hivTest;
    }

    public void setHivTest(TestResult hivTest) {
        this.hivTest = hivTest;
    }

    public TestResult getHbvTest() {
        return hbvTest;
    }

    public void setHbvTest(TestResult hbvTest) {
        this.hbvTest = hbvTest;
    }

    public TestResult getHcvTest() {
        return hcvTest;
    }

    public void setHcvTest(TestResult hcvTest) {
        this.hcvTest = hcvTest;
    }

    public Integer getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(Integer volumeMl) {
        this.volumeMl = volumeMl;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public Double getStorageTemperature() {
        return storageTemperature;
    }

    public void setStorageTemperature(Double storageTemperature) {
        this.storageTemperature = storageTemperature;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
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

    public ComponentType getDonationType() {
        return donationType;
    }

    public void setDonationType(ComponentType donationType) {
        this.donationType = donationType;
    }
}
