package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BloodUsage entity.
 * Tracks patient transfusion records.
 */
@Repository
public interface BloodUsageRepository extends JpaRepository<BloodUsage, Long> {

    /**
     * Find all usage records for a specific hospital.
     */
    List<BloodUsage> findByHospitalIdOrderByTransfusionDateDesc(Integer hospitalId);

    /**
     * Find all usage records for a specific blood component.
     */
    List<BloodUsage> findByBloodComponentId(Long bloodComponentId);

    /**
     * Find all usage records for a specific blood request.
     */
    List<BloodUsage> findByBloodRequestId(Long bloodRequestId);

    /**
     * Find usage records within a date range.
     */
    List<BloodUsage> findByTransfusionDateBetweenOrderByTransfusionDateDesc(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Count total transfusions by hospital.
     */
    long countByHospitalId(Integer hospitalId);
}
