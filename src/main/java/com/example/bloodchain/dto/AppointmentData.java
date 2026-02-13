package com.example.bloodchain.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for scheduling appointment data.
 */
public class AppointmentData {
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String notes;

    // Constructors
    public AppointmentData() {}

    public AppointmentData(LocalDate appointmentDate, LocalTime appointmentTime, String notes) {
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.notes = notes;
    }

    // Getters and Setters
    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
