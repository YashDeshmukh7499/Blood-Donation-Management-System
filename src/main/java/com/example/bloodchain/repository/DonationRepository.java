package com.example.bloodchain.repository;

import com.example.bloodchain.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByStatus(String status);
    List<Donation> findByEmail(String email);
    List<Donation> findByEmailOrderByDonationDateDesc(String email);
}
