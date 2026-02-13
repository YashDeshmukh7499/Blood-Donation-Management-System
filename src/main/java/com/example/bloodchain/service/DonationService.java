package com.example.bloodchain.service;

import com.example.bloodchain.model.Donation;
import com.example.bloodchain.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private com.example.bloodchain.repository.UserRepository userRepository;

    public List<com.example.bloodchain.dto.DonationDTO> getPendingDonationsDTO() {
        List<Donation> donations = donationRepository.findByStatus("Pending");
        return mapToDTO(donations);
    }

    public List<com.example.bloodchain.dto.DonationDTO> getAllDTO() {
        List<Donation> donations = donationRepository.findAll();
        return mapToDTO(donations);
    }

    private List<com.example.bloodchain.dto.DonationDTO> mapToDTO(List<Donation> donations) {
        return donations.stream().map(d -> {
            com.example.bloodchain.model.User user = userRepository.findById(d.getEmail()).orElse(null);
            String name = (user != null) ? user.getName() : "Unknown";
            String bloodGroup = (user != null) ? user.getBloodGroup() : "Unknown";
            
            com.example.bloodchain.dto.DonationDTO dto = new com.example.bloodchain.dto.DonationDTO(
                d.getId(), name, bloodGroup, d.getUnits(), d.getDonationDate(), d.getLocation(), d.getStatus(), d.getBlockHash()
            );
            // Simulate health check status (or fetch from somewhere if available)
            dto.setHealthStatus("Passed"); 
            return dto;
        }).collect(java.util.stream.Collectors.toList());
    }

    public Donation addDonation(Donation donation) {
        if (donation.getStatus() == null || donation.getStatus().isBlank()) {
            donation.setStatus("Pending");
        }
        return donationRepository.save(donation);
    }

    public List<Donation> getDonationsByEmail(String email) {
        return donationRepository.findByEmailOrderByDonationDateDesc(email);
    }

    public boolean approveDonation(Long id) {
        Optional<Donation> opt = donationRepository.findById(id);
        if (opt.isEmpty()) return false;

        Donation d = opt.get();
        d.setStatus("Verified");
        if (d.getBlockHash() == null || d.getBlockHash().isBlank()) {
            d.setBlockHash(UUID.randomUUID().toString().replace("-", ""));
        }
        donationRepository.save(d);
        return true;
    }

    public boolean rejectDonation(Long id) {
        Optional<Donation> opt = donationRepository.findById(id);
        if (opt.isEmpty()) return false;

        Donation d = opt.get();
        d.setStatus("Rejected");
        donationRepository.save(d);
        return true;
    }
}
