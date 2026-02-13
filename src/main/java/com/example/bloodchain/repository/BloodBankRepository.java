package com.example.bloodchain.repository;

import com.example.bloodchain.model.BloodBank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodBankRepository extends JpaRepository<BloodBank, Integer> {
    
    // 🔍 Search blood banks by city
    java.util.List<BloodBank> findByCityContainingIgnoreCase(String city);
}
