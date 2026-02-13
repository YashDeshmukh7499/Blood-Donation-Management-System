package com.example.bloodchain.repository;

import com.example.bloodchain.model.DonationRequest;
import com.example.bloodchain.model.DonationRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for DonationRequest entity.
 */
@Repository
public interface DonationRequestRepository extends JpaRepository<DonationRequest, Long> {

    /**
     * Find all donation requests by donor email.
     */
    List<DonationRequest> findByDonorEmailOrderByRequestDateDesc(String donorEmail);

    /**
     * Find all donation requests by donor ID.
     */
    List<DonationRequest> findByDonorIdOrderByRequestDateDesc(Integer donorId);

    /**
     * Find all donation requests by status.
     */
    List<DonationRequest> findByStatusOrderByRequestDateAsc(DonationRequestStatus status);

    /**
     * Count requests by status.
     */
    long countByStatus(DonationRequestStatus status);

    /**
     * Find all donation requests by blood bank ID.
     */
    List<DonationRequest> findByBloodBankIdOrderByRequestDateDesc(Integer bloodBankId);

    /**
     * Find all donation requests by blood bank ID and status.
     */
    List<DonationRequest> findByBloodBankIdAndStatusOrderByRequestDateDesc(
        Integer bloodBankId, DonationRequestStatus status);

    /**
     * Count active donation requests for a donor (PENDING, APPROVED, SCHEDULED).
     * Used to prevent multiple active requests from the same donor.
     */
    @Query("SELECT COUNT(dr) FROM DonationRequest dr " +
           "WHERE dr.donorEmail = :email " +
           "AND dr.status IN ('PENDING', 'APPROVED', 'SCHEDULED')")
    long countActiveRequests(@Param("email") String email);
}

