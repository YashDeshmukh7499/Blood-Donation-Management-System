package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // ✅ Primary key (auto-increment, unique)

    @Column(nullable = false)
    private String email; // linked to user email (foreign reference)

    @Column(nullable = false)
    private LocalDate donationDate;

    @Column(nullable = false)
    private int units; // in milliliters

    @Column(nullable = false)
    private String location; // where donation happened

    @Column(unique = true, nullable = false)
    private String blockHash; // blockchain record identifier

    @Column(nullable = false)
    private String status; // e.g., "Pending", "Verified", etc.


    // Constructors
    public Donation() {}

    public Donation(String email, LocalDate donationDate, int units, String location, String blockHash, String status) {
        this.email = email;
        this.donationDate = donationDate;
        this.units = units;
        this.location = location;
        this.blockHash = blockHash;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public LocalDate getDonationDate() { return donationDate; }

    public void setDonationDate(LocalDate donationDate) { this.donationDate = donationDate; }

    public int getUnits() { return units; }

    public void setUnits(int units) { this.units = units; }

    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    public String getBlockHash() { return blockHash; }

    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
