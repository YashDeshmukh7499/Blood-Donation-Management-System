package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodRequestComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for BloodRequestComponent entity.
 * Maps blood requests to assigned components.
 */
@Repository
public interface BloodRequestComponentRepository extends JpaRepository<BloodRequestComponent, Long> {

    /**
     * Find all component mappings for a blood request.
     */
    List<BloodRequestComponent> findByBloodRequestId(Long bloodRequestId);

    /**
     * Find all requests that used a specific component.
     */
    List<BloodRequestComponent> findByBloodComponentId(Long bloodComponentId);

    /**
     * Count components assigned to a request.
     */
    long countByBloodRequestId(Long bloodRequestId);
}
