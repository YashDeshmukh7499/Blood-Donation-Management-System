package com.example.bloodchain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "blood_banks")
public class BloodBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bankId;

    private String name;
    private String email;
    private String city;
    private int capacity;
    private int availableUnits;

    // 🆔 Identity Details
    private String licenseNumber;
    private String category; // Government / Private / Hospital
    private String establishedYear;
    
    // 📍 Location Details
    private String state;
    private String area;
    private String pincode;
    private Double latitude;
    private Double longitude;

    // 📞 Contact Details
    private String phone;
    private String website;

    // 🕒 Operational Details
    private String workingDays; // e.g., "Mon-Sat"
    private String operatingHours; // e.g., "9 AM - 6 PM"
    
    // ✅ Status & Verification
    private boolean verified = false; // Only Admin can verify
    private boolean acceptingDonations = true; // Blood Bank can toggle

    // ✅ Getters & Setters
    public int getBankId() { return bankId; }
    public void setBankId(int bankId) { this.bankId = bankId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAvailableUnits() { return availableUnits; }
    public void setAvailableUnits(int availableUnits) { this.availableUnits = availableUnits; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getEstablishedYear() { return establishedYear; }
    public void setEstablishedYear(String establishedYear) { this.establishedYear = establishedYear; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getWorkingDays() { return workingDays; }
    public void setWorkingDays(String workingDays) { this.workingDays = workingDays; }

    public String getOperatingHours() { return operatingHours; }
    public void setOperatingHours(String operatingHours) { this.operatingHours = operatingHours; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isAcceptingDonations() { return acceptingDonations; }
    public void setAcceptingDonations(boolean acceptingDonations) { this.acceptingDonations = acceptingDonations; }
}
