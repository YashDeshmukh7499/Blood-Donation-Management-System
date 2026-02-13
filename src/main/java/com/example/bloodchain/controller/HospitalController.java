package com.example.bloodchain.controller;

import com.example.bloodchain.model.Hospital;
import com.example.bloodchain.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hospital")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class HospitalController {

    @Autowired
    private com.example.bloodchain.service.BloodRequestService bloodRequestService;

    @Autowired
    private com.example.bloodchain.service.BloodUsageService bloodUsageService;

    @Autowired
    private com.example.bloodchain.repository.HospitalRepository hospitalRepository;

    @Autowired
    private com.example.bloodchain.service.BloodComponentService componentService;

    @Autowired
    private com.example.bloodchain.repository.UserRepository userRepository;

    @Autowired
    private com.example.bloodchain.repository.BloodComponentRepository componentRepository;

    // 🏥 Get Hospital Profile
    @GetMapping("/{email:.+}")
    public ResponseEntity<?> getHospitalProfile(@PathVariable String email) {
        Optional<Hospital> hospital = hospitalRepository.findByEmail(email);
        if (hospital.isPresent()) {
            return ResponseEntity.ok(hospital.get());
        }

        // Fallback: Check if User exists with role 'hospital' and auto-register
        com.example.bloodchain.model.User user = userRepository.findByEmail(email);
        if (user != null && "hospital".equalsIgnoreCase(user.getRole())) {
            Hospital newHospital = new Hospital();
            newHospital.setEmail(user.getEmail());
            newHospital.setName(user.getName() != null ? user.getName() : "New Hospital");
            newHospital.setCity(user.getCity() != null ? user.getCity() : "Unknown");
            newHospital.setContactNumber(user.getPhone() != null ? user.getPhone() : "N/A");
            newHospital.setTotalPatients(0);
            
            hospitalRepository.save(newHospital);
            return ResponseEntity.ok(newHospital);
        }

        return ResponseEntity.status(404).body("❌ Hospital not found");
    }

    // 🩸 Create Blood Request (Hospital -> Blood Bank)
    @PostMapping("/request/create")
    public ResponseEntity<?> createBloodRequest(@RequestBody com.example.bloodchain.model.BloodRequest request) {
        try {
            // Find hospital ID from email
            Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(request.getHospitalEmail());
            
            if (hospitalOpt.isEmpty()) {
                // Try to find user and auto-create hospital record
                com.example.bloodchain.model.User user = userRepository.findByEmail(request.getHospitalEmail());
                if (user != null && "hospital".equalsIgnoreCase(user.getRole())) {
                    Hospital newHospital = new Hospital();
                    newHospital.setEmail(user.getEmail());
                    newHospital.setName(user.getName() != null ? user.getName() : "New Hospital");
                    newHospital.setCity(user.getCity() != null ? user.getCity() : "Unknown");
                    newHospital.setContactNumber(user.getPhone() != null ? user.getPhone() : "N/A");
                    newHospital.setTotalPatients(0);
                    hospitalOpt = Optional.of(hospitalRepository.save(newHospital));
                } else {
                    return ResponseEntity.badRequest().body("❌ Hospital not registered with this email.");
                }
            }
            
            request.setHospitalId(hospitalOpt.get().getHospitalId());
            
            com.example.bloodchain.model.BloodRequest created = bloodRequestService.createBloodRequest(request);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Failed to create request: " + e.getMessage());
        }
    }

    // 📋 Get Hospital's Blood Requests
    @GetMapping("/requests/{email}")
    public List<com.example.bloodchain.model.BloodRequest> getHospitalRequests(@PathVariable String email) {
        return bloodRequestService.findByHospitalEmail(email);
    }

    // 💉 Record Blood Usage (Transfusion)
    @PostMapping("/usage/record")
    public ResponseEntity<?> recordBloodUsage(@RequestBody com.example.bloodchain.model.BloodUsage usage) {
        try {
            // Hardcoded "Doctor" for now, ideally comes from auth token
            com.example.bloodchain.model.BloodUsage recorded = bloodUsageService.recordTransfusion(usage, "Hospital Staff");
            return ResponseEntity.ok(recorded);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("❌ Failed to record usage: " + e.getMessage());
        }
    }

    // 📜 Get Hospital's Usage History
    @GetMapping("/usage/history/{hospitalId}")
    public List<com.example.bloodchain.model.BloodUsage> getHospitalUsage(@PathVariable Integer hospitalId) {
        return bloodUsageService.findByHospitalId(hospitalId);
    }

    // 🏥 Get Hospital's Current Inventory Summary
    @GetMapping("/inventory/summary")
    public ResponseEntity<List<java.util.Map<String, Object>>> getHospitalInventorySummary(@RequestParam String email) {
        List<Object[]> rawCounts = componentRepository.countHospitalInventoryGrouped(email);
        
        // Define all blood groups to ensure even 0 counts are returned
        java.util.Map<String, Integer> countsMap = new java.util.HashMap<>();
        String[] allGroups = {"A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
        for (String bg : allGroups) countsMap.put(bg, 0);
        
        // Populate actual counts
        for (Object[] row : rawCounts) {
            String bg = (String) row[0];
            Long count = (Long) row[1];
            countsMap.put(bg, count.intValue());
        }
        
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (String bg : allGroups) {
            int count = countsMap.get(bg);
            String status = count >= 10 ? "Adequate" : (count >= 5 ? "Low" : "Critical");
            String statusClass = count >= 10 ? "status-adequate" : (count >= 5 ? "status-low" : "status-critical");
            
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("bloodGroup", bg);
            map.put("units", count);
            map.put("status", status);
            map.put("statusClass", statusClass); // Helper for frontend class
            result.add(map);
        }
        
        return ResponseEntity.ok(result);
    }

    // 📦 Get Available Inventory (for Hospital View)
    @GetMapping("/inventory/available")
    public List<com.example.bloodchain.model.BloodComponent> getAvailableInventory(
            @RequestParam com.example.bloodchain.model.ComponentType type,
            @RequestParam String bloodGroup) {
        // Return max 10 to avoid data dumping
        return componentService.findAvailableComponents(type, bloodGroup, 10);
    }

    // 📊 Get Hospital Usage Analytics (for Chart)
    @GetMapping("/usage/analytics")
    public ResponseEntity<?> getUsageAnalytics(@RequestParam String email, @RequestParam(defaultValue = "7") int days) {
        Optional<Hospital> hospitalOpt = hospitalRepository.findByEmail(email);
        if (hospitalOpt.isEmpty()) {
            return ResponseEntity.status(404).body("❌ Hospital not found");
        }
        
        java.util.Map<String, Long> analytics = bloodUsageService.getUsageAnalytics(hospitalOpt.get().getHospitalId(), days);
        return ResponseEntity.ok(analytics);
    }
}
