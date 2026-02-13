package com.example.bloodchain.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "donors")
public class Donor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int donorId;

    private String name;
    private String email;
    private String bloodGroup;
    private String city;
    private LocalDate lastDonation;

    // ✅ Getters & Setters
    public int getDonorId() { return donorId; }
    public void setDonorId(int donorId) { this.donorId = donorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public LocalDate getLastDonation() { return lastDonation; }
    public void setLastDonation(LocalDate lastDonation) { this.lastDonation = lastDonation; }
}
