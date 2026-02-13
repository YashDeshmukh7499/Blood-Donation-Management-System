package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing individual blood components after separation.
 * One blood unit can be separated into multiple components (RBC, Plasma, Platelets).
 * Each component has its own expiry date and storage requirements.
 */
@Entity
@Table(name = "blood_components")
public class BloodComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String componentId;  // e.g., "RBC-2025-000001"

    @Column(name = "blood_unit_id", nullable = false)
    private Long bloodUnitId;  // Foreign key to blood_units

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type", nullable = false, length = 20)
    private ComponentType componentType;

    @Column(name = "separation_date", nullable = false)
    private LocalDate separationDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;  // Calculated based on component type

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComponentStatus status = ComponentStatus.AVAILABLE;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "storage_type", length = 100)
    private String storageType;  // e.g., "Refrigerator (2-6°C)", "Freezer (-18°C)"

    @Column(name = "storage_temperature")
    private Double storageTemperature;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public BloodComponent() {}

    public BloodComponent(String componentId, Long bloodUnitId, ComponentType componentType,
                         LocalDate separationDate, LocalDate expiryDate) {
        this.componentId = componentId;
        this.bloodUnitId = bloodUnitId;
        this.componentType = componentType;
        this.separationDate = separationDate;
        this.expiryDate = expiryDate;
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

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Long getBloodUnitId() {
        return bloodUnitId;
    }

    public void setBloodUnitId(Long bloodUnitId) {
        this.bloodUnitId = bloodUnitId;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public LocalDate getSeparationDate() {
        return separationDate;
    }

    public void setSeparationDate(LocalDate separationDate) {
        this.separationDate = separationDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public ComponentStatus getStatus() {
        return status;
    }

    public void setStatus(ComponentStatus status) {
        this.status = status;
    }

    public Integer getVolumeMl() {
        return volumeMl;
    }

    public void setVolumeMl(Integer volumeMl) {
        this.volumeMl = volumeMl;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public Double getStorageTemperature() {
        return storageTemperature;
    }

    public void setStorageTemperature(Double storageTemperature) {
        this.storageTemperature = storageTemperature;
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
