package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodUnit;
import com.example.bloodchain.model.BloodUnitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BloodUnit entity.
 * Provides database operations for blood unit management.
 */
@Repository
public interface BloodUnitRepository extends JpaRepository<BloodUnit, Long> {

    /**
     * Find blood unit by unique blood unit ID.
     */
    Optional<BloodUnit> findByBloodUnitId(String bloodUnitId);
    
    // Find unit by donation request ID
    Optional<BloodUnit> findByDonationRequestId(Long donationRequestId);

    /**
     * Find all blood units by donor ID.
     */
    List<BloodUnit> findByDonorId(Integer donorId);

    /**
     * Find all blood units by status.
     */
    List<BloodUnit> findByStatus(BloodUnitStatus status);

    /**
     * Find all blood units by blood group and status.
     */
    List<BloodUnit> findByBloodGroupAndStatus(String bloodGroup, BloodUnitStatus status);

    /**
     * Find expired blood units that are not yet marked as expired.
     */
    List<BloodUnit> findByExpiryDateBeforeAndStatusNot(LocalDate date, BloodUnitStatus status);

    /**
     * Count blood units by status.
     */
    long countByStatus(BloodUnitStatus status);

    /**
     * Check if blood unit ID already exists.
     */
    boolean existsByBloodUnitId(String bloodUnitId);
}
