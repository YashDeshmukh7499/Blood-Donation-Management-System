package com.example.bloodchain.dto;

/**
 * DTO for donation completion data.
 */
public class CompletionData {
    private Integer bloodBankId;
    private String bloodBankName;
    private Integer unitsCollected; // in mL
    private String testNotes;
    private String storageLocation;

    // Constructors
    public CompletionData() {}

    public CompletionData(Integer bloodBankId, String bloodBankName, Integer unitsCollected) {
        this.bloodBankId = bloodBankId;
        this.bloodBankName = bloodBankName;
        this.unitsCollected = unitsCollected;
    }

    // Getters and Setters
    public Integer getBloodBankId() {
        return bloodBankId;
    }

    public void setBloodBankId(Integer bloodBankId) {
        this.bloodBankId = bloodBankId;
    }

    public String getBloodBankName() {
        return bloodBankName;
    }

    public void setBloodBankName(String bloodBankName) {
        this.bloodBankName = bloodBankName;
    }

    public Integer getUnitsCollected() {
        return unitsCollected;
    }

    public void setUnitsCollected(Integer unitsCollected) {
        this.unitsCollected = unitsCollected;
    }

    public String getTestNotes() {
        return testNotes;
    }

    public void setTestNotes(String testNotes) {
        this.testNotes = testNotes;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
