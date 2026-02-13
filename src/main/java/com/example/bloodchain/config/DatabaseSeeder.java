package com.example.bloodchain.config;

import com.example.bloodchain.model.BloodBank;
import com.example.bloodchain.repository.BloodBankRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(BloodBankRepository bloodBankRepository) {
        return args -> {
            // Check if Blood Bank with ID 1 exists
            if (bloodBankRepository.count() == 0) {
                System.out.println("🌱 Seeding default Blood Bank data...");
                
                BloodBank bank = new BloodBank();
                // Basic Identity
                bank.setName("RedLife Blood Center");
                bank.setEmail("info@redlife.org");
                // bank.setPassword("password123"); // Password handled by User entity
                bank.setLicenseNumber("BB-2024-001-MH");
                bank.setCategory("Private");
                bank.setEstablishedYear("2010"); // Changed to String
                
                // Location
                bank.setState("Maharashtra");
                bank.setCity("Mumbai");
                bank.setArea("Andheri West");
                bank.setPincode("400053");
                bank.setLatitude(19.1136);
                bank.setLongitude(72.8697);
                
                // Contact & Operations
                bank.setPhone("+91 98765 43210");
                bank.setWebsite("www.redlife.org");
                bank.setOperatingHours("09:00 - 18:00");
                bank.setWorkingDays("Mon, Tue, Wed, Thu, Fri, Sat");
                
                // Status
                bank.setVerified(true);
                bank.setAcceptingDonations(true);
                
                bloodBankRepository.save(bank);
                
                System.out.println("✅ Default Blood Bank (ID: 1) created successfully!");
            } else {
                System.out.println("ℹ️ Database already contains Blood Bank data. Skipping seed.");
            }
        };
    }
}
