package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodTrackingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BloodTrackingLog entity.
 * CRITICAL: This repository should ONLY support INSERT operations.
 * No UPDATE or DELETE operations should be performed on audit logs.
 */
@Repository
public interface BloodTrackingLogRepository extends JpaRepository<BloodTrackingLog, Long> {

    /**
     * Find all tracking logs for a specific blood unit, ordered by timestamp.
     * This provides the complete audit trail.
     */
    List<BloodTrackingLog> findByBloodUnitIdOrderByTimestampAsc(String bloodUnitId);

    /**
     * Find all actions performed by a specific user.
     */
    List<BloodTrackingLog> findByPerformedByOrderByTimestampDesc(String performedBy);

    /**
     * Find all logs for a specific action type.
     */
    List<BloodTrackingLog> findByActionOrderByTimestampDesc(String action);

    /**
     * Find logs within a date range.
     */
    List<BloodTrackingLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    // Get latest log for chain linking
    BloodTrackingLog findTopByOrderByTimestampDesc();

    // Get latest log for specific unit (for current hash)
    BloodTrackingLog findTopByBloodUnitIdOrderByTimestampDesc(String bloodUnitId);

    /**
     * Find recent logs (for admin dashboard).
     */
    List<BloodTrackingLog> findTop100ByOrderByTimestampDesc();
}
