package com.example.bloodchain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled tasks for blood management system.
 * Runs automated maintenance tasks like expiry management.
 */
@Service
public class ScheduledTasks {

    @Autowired
    private BloodUnitService bloodUnitService;

    @Autowired
    private BloodComponentService componentService;

    /**
     * Mark expired blood units.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void markExpiredBloodUnits() {
        System.out.println("Running scheduled task: Mark expired blood units");
        bloodUnitService.markExpiredBloodUnits();
    }

    /**
     * Mark expired blood components.
     * Runs daily at 2:00 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void markExpiredComponents() {
        System.out.println("Running scheduled task: Mark expired components");
        componentService.markExpiredComponents();
    }

    // TODO: Add email alerts for expiring blood (3 days before expiry)
    // @Scheduled(cron = "0 0 9 * * *")  // Daily at 9 AM
    // public void sendExpiryAlerts() { ... }
}
