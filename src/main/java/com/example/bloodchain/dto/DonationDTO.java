package com.example.bloodchain.dto;

import java.time.LocalDate;

public class DonationDTO {
    private Long id;
    private String donorName;
    private String bloodGroup;
    private int units;
    private LocalDate donationDate;
    private String location;
    private String status;
    private String blockHash;
    private String healthStatus; // New field for pending approvals

    public DonationDTO(Long id, String donorName, String bloodGroup, int units, LocalDate donationDate, String location, String status, String blockHash) {
        this.id = id;
        this.donorName = donorName;
        this.bloodGroup = bloodGroup;
        this.units = units;
        this.donationDate = donationDate;
        this.location = location;
        this.status = status;
        this.blockHash = blockHash;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getDonorName() { return donorName; }
    public void setDonorName(String donorName) { this.donorName = donorName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getUnits() { return units; }
    public void setUnits(int units) { this.units = units; }

    public LocalDate getDonationDate() { return donationDate; }
    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
}
