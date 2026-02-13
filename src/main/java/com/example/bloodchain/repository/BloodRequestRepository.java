package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodRequest;
import com.example.bloodchain.model.BloodRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BloodRequest entity.
 */
@Repository
public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {

    /**
     * Find blood request by unique request number.
     */
    Optional<BloodRequest> findByRequestNumber(String requestNumber);

    /**
     * Find all blood requests by hospital ID.
     */
    List<BloodRequest> findByHospitalIdOrderByRequestDateDesc(Integer hospitalId);

    /**
     * Find all blood requests by hospital email.
     */
    List<BloodRequest> findByHospitalEmailOrderByRequestDateDesc(String hospitalEmail);

    /**
     * Find all blood requests by status.
     */
    List<BloodRequest> findByStatusOrderByRequestDateDesc(BloodRequestStatus status);

    /**
     * Find pending requests (for blood bank dashboard).
     */
    List<BloodRequest> findByStatusOrderByUrgencyDescRequestDateAsc(BloodRequestStatus status);

    /**
     * Count requests by status.
     */
    long countByStatus(BloodRequestStatus status);

    /**
     * Check if request number already exists.
     */
    boolean existsByRequestNumber(String requestNumber);
}
