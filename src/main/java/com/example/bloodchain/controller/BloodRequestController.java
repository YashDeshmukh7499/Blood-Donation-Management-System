package com.example.bloodchain.controller;

import com.example.bloodchain.model.BloodRequest;
import com.example.bloodchain.model.RequestUrgency;
import com.example.bloodchain.repository.BloodRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/blood-requests")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class BloodRequestController {

    @Autowired
    private BloodRequestRepository bloodRequestRepo;

    // 🚨 Get Active Emergency Requests (Privacy-Safe)
    @GetMapping("/emergency")
    public ResponseEntity<List<Map<String, Object>>> getEmergencyRequests() {
        // Fetch all requests - ideally filter by urgency in DB query
        List<BloodRequest> allRequests = bloodRequestRepo.findAll();
        
        List<Map<String, Object>> emergencyRequests = allRequests.stream()
            .filter(req -> req.getUrgency() == RequestUrgency.EMERGENCY)
            // Filter out completed/fulfilled requests if status exists
            // .filter(req -> req.getStatus() == BloodRequestStatus.REQUESTED) 
            .map(req -> {
                Map<String, Object> map = new HashMap<>();
                map.put("requestId", req.getId());
                map.put("bloodGroup", req.getBloodGroup());
                map.put("urgency", req.getUrgency().toString());
                map.put("location", "City Hospital (Hidden for Privacy)"); // Privacy masking
                // ideally location should come from hospital entity but for now static or minimal
                map.put("hospitalId", req.getHospitalId()); // Exposed but name is safer
                return map;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(emergencyRequests);
    }
    @Autowired
    private com.example.bloodchain.service.BloodRequestService bloodRequestService;

    // ✅ Approve Request
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        try {
            bloodRequestService.approveBloodRequest(id, "Blood Bank Staff"); // TODO: Get logged in user
            return ResponseEntity.ok("Request Approved Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ❌ Reject Request
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
        try {
            String reason = (payload != null && payload.containsKey("reason")) ? payload.get("reason") : "Insufficient Stock / Policy";
            bloodRequestService.rejectBloodRequest(id, reason, "Blood Bank Staff"); // TODO: Get logged in user
            return ResponseEntity.ok("Request Rejected Successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // 🚚 Confirm Receipt (Hospital)
    @PutMapping("/{id}/confirm-receipt")
    public ResponseEntity<String> confirmReceipt(@PathVariable Long id) {
        try {
            bloodRequestService.confirmReceipt(id, "Hospital Staff"); // TODO: Get logged in user
            return ResponseEntity.ok("Receipt Confirmed & Inventory Updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
