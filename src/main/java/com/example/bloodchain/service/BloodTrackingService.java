package com.example.bloodchain.service;

import com.example.bloodchain.model.BloodTrackingLog;
import com.example.bloodchain.repository.BloodTrackingLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CRITICAL SERVICE: Manages immutable audit trail for blood units.
 * This service implements the blockchain event logging equivalent.
 * All operations are INSERT-only, no UPDATE or DELETE allowed.
 */
@Service
public class BloodTrackingService {

    @Autowired
    private BloodTrackingLogRepository trackingLogRepository;

    /**
     * Log an action on a blood unit.
     * This is the ONLY way to create audit log entries.
     * 
     * @param bloodUnitId Unique blood unit identifier
     * @param action Action performed (e.g., "BLOOD_COLLECTED", "BLOOD_TESTED")
     * @param performedBy Email of user who performed the action
     * @param role Role of the user (ROLE_DONOR, ROLE_BLOODBANK, etc.)
     * @param details Additional context or JSON data
     */
    public void logAction(String bloodUnitId, String action, String performedBy, 
                         String role, String details) {
        BloodTrackingLog log = new BloodTrackingLog();
        log.setBloodUnitId(bloodUnitId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(role);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        finalizeAndSave(log);
    }

    /**
     * Log a status change with previous and new status.
     */
    public void logStatusChange(String bloodUnitId, String action, String performedBy,
                               String role, String previousStatus, String newStatus, 
                               String details) {
        BloodTrackingLog log = new BloodTrackingLog();
        log.setBloodUnitId(bloodUnitId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedByRole(role);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        
        finalizeAndSave(log);
    }

    private void finalizeAndSave(BloodTrackingLog log) {
        // Link to previous block (Global Chain)
        BloodTrackingLog lastLog = trackingLogRepository.findTopByOrderByTimestampDesc();
        if (lastLog != null) {
            log.setPreviousHash(lastLog.getHash() != null ? lastLog.getHash() : "0");
        } else {
            log.setPreviousHash("0"); // Genesis block
        }
        
        // Calculate current hash
        log.setHash(calculateHash(log));
        
        trackingLogRepository.save(log);
    }

    private String calculateHash(BloodTrackingLog log) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String data = (log.getBloodUnitId() != null ? log.getBloodUnitId() : "") +
                          (log.getAction() != null ? log.getAction() : "") +
                          (log.getTimestamp() != null ? log.getTimestamp().toString() : "") +
                          (log.getPreviousHash() != null ? log.getPreviousHash() : "0") +
                          (log.getDetails() != null ? log.getDetails() : "");
            
            byte[] encodedhash = digest.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Get complete audit trail for a blood unit.
     * Returns all logs ordered by timestamp (oldest first).
     * This provides end-to-end traceability from donor to patient.
     */
    public List<BloodTrackingLog> getBloodHistory(String bloodUnitId) {
        return trackingLogRepository.findByBloodUnitIdOrderByTimestampAsc(bloodUnitId);
    }

    /**
     * Get all actions performed by a specific user.
     * Useful for accountability and user activity tracking.
     */
    public List<BloodTrackingLog> getUserActivity(String userEmail) {
        return trackingLogRepository.findByPerformedByOrderByTimestampDesc(userEmail);
    }

    /**
     * Get all logs for a specific action type.
     */
    public List<BloodTrackingLog> getActionLogs(String action) {
        return trackingLogRepository.findByActionOrderByTimestampDesc(action);
    }

    /**
     * Get logs within a date range (for reports).
     */
    public List<BloodTrackingLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return trackingLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
    }

    /**
     * Get recent logs for admin dashboard.
     */
    public List<BloodTrackingLog> getRecentLogs() {
        return trackingLogRepository.findTop100ByOrderByTimestampDesc();
    }
}
