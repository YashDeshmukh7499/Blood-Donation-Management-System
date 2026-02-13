package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodComponent;
import com.example.bloodchain.model.ComponentStatus;
import com.example.bloodchain.model.ComponentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BloodComponent entity.
 * Provides database operations for blood component management.
 */
@Repository
public interface BloodComponentRepository extends JpaRepository<BloodComponent, Long> {

    /**
     * Find blood component by unique component ID.
     */
    Optional<BloodComponent> findByComponentId(String componentId);

    /**
     * Find all components for a specific blood unit.
     */
    List<BloodComponent> findByBloodUnitId(Long bloodUnitId);

    /**
     * Find all components by status.
     */
    List<BloodComponent> findByStatus(ComponentStatus status);

    /**
     * Count components by type.
     */
    long countByComponentType(ComponentType componentType);

    /**
     * Find expired components that are not yet marked as expired.
     */
    List<BloodComponent> findByExpiryDateBeforeAndStatusNot(LocalDate date, ComponentStatus status);

    /**
     * Find available components by type (for hospital requests).
     * Joins with blood_units table to get blood group.
     */
    @Query("SELECT bc FROM BloodComponent bc, BloodUnit bu " +
           "WHERE bc.bloodUnitId = bu.id " +
           "AND bc.componentType = :componentType " +
           "AND bu.bloodGroup = :bloodGroup " +
           "AND bc.status = 'AVAILABLE' " +
           "AND bc.expiryDate > :today " +
           "ORDER BY bc.expiryDate ASC")
    List<BloodComponent> findAvailableComponents(
        @Param("componentType") ComponentType componentType,
        @Param("bloodGroup") String bloodGroup,
        @Param("today") LocalDate today
    );

    /**
     * Count available components by type and blood group.
     */
    @Query("SELECT COUNT(bc) FROM BloodComponent bc, BloodUnit bu " +
           "WHERE bc.bloodUnitId = bu.id " +
           "AND bc.componentType = :componentType " +
           "AND bu.bloodGroup = :bloodGroup " +
           "AND bc.status = 'AVAILABLE' " +
           "AND bc.expiryDate > :today")
    long countAvailableComponents(
        @Param("componentType") ComponentType componentType,
        @Param("bloodGroup") String bloodGroup,
        @Param("today") LocalDate today
    );


    /**
     * Count components received by a specific hospital, grouped by blood group.
     */
    @Query("SELECT bu.bloodGroup, COUNT(bc) FROM BloodComponent bc " +
           "JOIN BloodRequestComponent brc ON bc.id = brc.bloodComponentId " +
           "JOIN BloodRequest br ON brc.bloodRequestId = br.id " +
           "JOIN BloodUnit bu ON bc.bloodUnitId = bu.id " +
           "WHERE br.hospitalEmail = :hospitalEmail " +
           "AND bc.status = 'RECEIVED' " +
           "GROUP BY bu.bloodGroup")
    List<Object[]> countHospitalInventoryGrouped(
        @Param("hospitalEmail") String hospitalEmail
    );
}
