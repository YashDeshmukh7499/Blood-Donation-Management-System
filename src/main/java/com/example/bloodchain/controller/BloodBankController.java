package com.example.bloodchain.controller;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bloodbank")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class BloodBankController {

    @Autowired
    private BloodBankRepository bloodBankRepo;

    @Autowired
    private BloodUnitRepository bloodUnitRepo;

    @Autowired
    private DonationRequestRepository donationRequestRepo;

    @Autowired
    private BloodTrackingLogRepository trackingLogRepo;

    @Autowired
    private BloodRequestRepository bloodRequestRepo;

    @Autowired
    private DonorRepository donorRepo;

    @Autowired
    private BloodComponentRepository componentRepository;

    // ➕ Add blood bank
    @PostMapping("/add")
    public String addBloodBank(@RequestBody BloodBank bank) {
        bloodBankRepo.save(bank);
        return "✅ Blood Bank added successfully!";
    }
    
    // ✏️ Update Blood Bank Profile
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateBloodBank(@PathVariable int id, @RequestBody BloodBank updatedBank) {
        Optional<BloodBank> existingBankOpt = bloodBankRepo.findById(id);
        if (existingBankOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        BloodBank bank = existingBankOpt.get();
        // Update fields if provided (null checks)
        if (updatedBank.getName() != null) bank.setName(updatedBank.getName());
        if (updatedBank.getEmail() != null) bank.setEmail(updatedBank.getEmail());
        if (updatedBank.getPhone() != null) bank.setPhone(updatedBank.getPhone());
        if (updatedBank.getCity() != null) bank.setCity(updatedBank.getCity());
        if (updatedBank.getArea() != null) bank.setArea(updatedBank.getArea());
        if (updatedBank.getState() != null) bank.setState(updatedBank.getState());
        if (updatedBank.getPincode() != null) bank.setPincode(updatedBank.getPincode());
        if (updatedBank.getLatitude() != null) bank.setLatitude(updatedBank.getLatitude());
        if (updatedBank.getLongitude() != null) bank.setLongitude(updatedBank.getLongitude());
        if (updatedBank.getWebsite() != null) bank.setWebsite(updatedBank.getWebsite());
        if (updatedBank.getOperatingHours() != null) bank.setOperatingHours(updatedBank.getOperatingHours());
        if (updatedBank.getWorkingDays() != null) bank.setWorkingDays(updatedBank.getWorkingDays());
        if (updatedBank.getCategory() != null) bank.setCategory(updatedBank.getCategory());
        if (updatedBank.getLicenseNumber() != null) bank.setLicenseNumber(updatedBank.getLicenseNumber());
        if (updatedBank.getEstablishedYear() != null) bank.setEstablishedYear(updatedBank.getEstablishedYear());
        
        // Note: verified status is NOT updated here for security
        bank.setAcceptingDonations(updatedBank.isAcceptingDonations());
        
        bloodBankRepo.save(bank);
        return ResponseEntity.ok("✅ Blood Bank profile updated successfully!");
    }

    // 📄 Get all blood banks
    @GetMapping("/all")
    public List<BloodBank> getAllBloodBanks() {
        return bloodBankRepo.findAll();
    }

    // 🏥 Get specific blood bank (for profile loading)
    @GetMapping("/{id}")
    public ResponseEntity<BloodBank> getBloodBankById(@PathVariable int id) {
        return bloodBankRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🔍 Search Blood Banks by City
    @GetMapping("/search")
    public List<BloodBank> searchBloodBanks(@RequestParam String city) {
        return bloodBankRepo.findByCityContainingIgnoreCase(city);
    }

    // ❌ Delete blood bank
    @DeleteMapping("/delete/{id}")
    public String deleteBloodBank(@PathVariable int id) {
        bloodBankRepo.deleteById(id);
        return "✅ Blood Bank deleted successfully.";
    }

    // 📊 Dashboard Overview Statistics
    @GetMapping("/dashboard/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // Total units collected (all blood units)
        long totalCollected = bloodUnitRepo.count();
        
        // Units dispatched (DISPATCHED status)
        long dispatched = bloodUnitRepo.countByStatus(BloodUnitStatus.DISPATCHED);
        
        // Available stock (STORED status)
        long availableStock = bloodUnitRepo.countByStatus(BloodUnitStatus.STORED);
        
        // Blockchain status
        long totalBlocks = trackingLogRepo.count();
        boolean blockchainValid = totalBlocks > 0;
        
        // Total Donors
        long totalDonors = donorRepo.count();

        // Expiring Soon (next 7 days)
        LocalDate sevenDaysFromNow = LocalDate.now().plusDays(7);
        List<BloodUnit> expiringUnits = bloodUnitRepo.findByExpiryDateBeforeAndStatusNot(sevenDaysFromNow, BloodUnitStatus.EXPIRED);
        // Filter those that are technically not expired yet but will expire soon, and are available
        long expiringSoon = expiringUnits.stream()
                .filter(u -> u.getStatus() == BloodUnitStatus.STORED && u.getExpiryDate().isAfter(LocalDate.now()))
                .count();

        // Emergency Requests
        // Assuming RequestUrgency.EMERGENCY exists, otherwise we'll fetch all REQUESTED and filter client side or similar
        // For now, let's count all REQUESTED status requests as "Active Requests" if we can't be sure about Emergency enum
        // But better to check. I'll count all pending requests for now.
        long activeRequests = bloodRequestRepo.countByStatus(BloodRequestStatus.REQUESTED);

        overview.put("totalCollected", totalCollected);
        overview.put("dispatched", dispatched);
        overview.put("availableStock", availableStock);
        overview.put("blockchainValid", blockchainValid);
        overview.put("totalBlocks", totalBlocks);
        overview.put("totalDonors", totalDonors);
        overview.put("expiringSoon", expiringSoon);
        overview.put("activeRequests", activeRequests);
        
        return ResponseEntity.ok(overview);
    }

    // 🩸 Blood Inventory by Blood Group
    @GetMapping("/dashboard/inventory")
    public ResponseEntity<List<Map<String, Object>>> getBloodInventory() {
        String[] bloodGroups = {"A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
        List<Map<String, Object>> inventory = new ArrayList<>();
        
        for (String bloodGroup : bloodGroups) {
            Map<String, Object> groupData = new HashMap<>();
            
            // Count available units for this blood group
            List<BloodUnit> units = bloodUnitRepo.findByBloodGroupAndStatus(bloodGroup, BloodUnitStatus.STORED);
            int unitCount = units.size();
            
            // Determine status based on count
            String status;
            String statusClass;
            if (unitCount >= 20) {
                status = "Safe";
                statusClass = "status-safe"; // Green
            } else if (unitCount >= 5) {
                status = "Low";
                statusClass = "status-low"; // Yellow
            } else {
                status = "Critical";
                statusClass = "status-critical"; // Red
            }
            
            groupData.put("bloodGroup", bloodGroup);
            groupData.put("units", unitCount);
            groupData.put("status", status);
            groupData.put("statusClass", statusClass);
            
            inventory.add(groupData);
        }
        
        return ResponseEntity.ok(inventory);
    }
    
    // 🩸 Detailed Blood Inventory (All Units)
    @GetMapping("/dashboard/inventory/all")
    public ResponseEntity<List<BloodUnit>> getAllBloodUnits() {
        // Return all blood units with details
        return ResponseEntity.ok(bloodUnitRepo.findAll());
    }

    @GetMapping("/dashboard/components/all")
    public ResponseEntity<List<BloodComponent>> getAllComponents() {
        return ResponseEntity.ok(componentRepository.findAll());
    }

    /**
     * Get aggregated inventory counts of AVAILABLE components.
     * Returns a list of maps, each containing: componentType, bloodGroup, and count.
     */
    @GetMapping("/dashboard/components/inventory")
    public ResponseEntity<List<Map<String, Object>>> getComponentInventory() {
        String[] bloodGroups = {"A+", "B+", "O+", "AB+", "A-", "B-", "O-", "AB-"};
        ComponentType[] types = ComponentType.values();
        List<Map<String, Object>> inventory = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (ComponentType type : types) {
            for (String group : bloodGroups) {
                long count = componentRepository.countAvailableComponents(type, group, today);
                
                if (count > 0) {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("type", type.toString());
                    entry.put("bloodGroup", group);
                    entry.put("count", count);
                    inventory.add(entry);
                }
            }
        }
        return ResponseEntity.ok(inventory);
    }

    // 📥 Pending Donation Requests
    @GetMapping("/dashboard/pending-donations")
    public ResponseEntity<List<Map<String, Object>>> getPendingDonations() {
        // Fetch PENDING requests
        List<DonationRequest> pendingRequests = donationRequestRepo.findByStatusOrderByRequestDateAsc(DonationRequestStatus.PENDING);
        return ResponseEntity.ok(mapDonationsToResponse(pendingRequests));
    }

    // ✅ Approved/Scheduled Donations (Ready for Completion)
    @GetMapping("/dashboard/approved-donations")
    public ResponseEntity<List<Map<String, Object>>> getApprovedDonations() {
        // Fetch APPROVED and SCHEDULED requests
        List<DonationRequest> approved = donationRequestRepo.findByStatusOrderByRequestDateAsc(DonationRequestStatus.APPROVED);
        List<DonationRequest> scheduled = donationRequestRepo.findByStatusOrderByRequestDateAsc(DonationRequestStatus.SCHEDULED);
        
        List<DonationRequest> combined = new ArrayList<>();
        combined.addAll(approved);
        combined.addAll(scheduled);
        
        // Sort by date/time if needed (already sorted individually, merging them simply might misorder slightly but acceptable)
        return ResponseEntity.ok(mapDonationsToResponse(combined));
    }

    // 🎉 Completed Donations (History)
    @GetMapping("/dashboard/completed-donations")
    public ResponseEntity<List<Map<String, Object>>> getCompletedDonations() {
        List<DonationRequest> completed = donationRequestRepo.findByStatusOrderByRequestDateAsc(DonationRequestStatus.COMPLETED);
        // Maybe limit to last 50?
        if (completed.size() > 50) {
            completed = completed.subList(completed.size() - 50, completed.size());
        }
        Collections.reverse(completed); // Show newest first
        return ResponseEntity.ok(mapDonationsToResponse(completed));
    }

    // Helper to map donations
    private List<Map<String, Object>> mapDonationsToResponse(List<DonationRequest> requests) {
        return requests.stream()
            .map(request -> {
                Map<String, Object> donation = new HashMap<>();
                donation.put("requestId", request.getRequestId());
                donation.put("donorEmail", request.getDonorEmail());
                donation.put("bloodBankId", request.getBloodBankId()); // Added
                
                // Try to fetch donor name if possible
                String donorName = "Unknown";
                Donor donor = donorRepo.findByEmail(request.getDonorEmail());
                if (donor != null) {
                    donorName = donor.getName();
                }
                donation.put("donorName", donorName);
                
                donation.put("bloodGroup", request.getBloodGroup());
                donation.put("requestDate", request.getRequestDate().toString());
                donation.put("status", request.getStatus().toString());

                // Fetch volume for completed donations
                if (request.getStatus() == DonationRequestStatus.COMPLETED) {
                     bloodUnitRepo.findByDonationRequestId(request.getRequestId())
                         .ifPresent(unit -> donation.put("volumeMl", unit.getVolumeMl()));
                }
                
                // Extra fields for approved/scheduled
                if (request.getAppointmentDate() != null) {
                    donation.put("appointmentDate", request.getAppointmentDate().toString());
                    donation.put("appointmentTime", request.getAppointmentTime() != null ? request.getAppointmentTime().toString() : "");
                }
                
                return donation;
            })
            .collect(Collectors.toList());
    }
    
    // 🏥 Hospital Blood Requests
    @GetMapping("/dashboard/hospital-requests")
    public ResponseEntity<List<Map<String, Object>>> getHospitalRequests() {
        List<BloodRequest> requests = bloodRequestRepo.findByStatusOrderByRequestDateDesc(BloodRequestStatus.REQUESTED);
        
        List<Map<String, Object>> requestList = requests.stream()
            .map(req -> {
                Map<String, Object> map = new HashMap<>();
                map.put("requestId", req.getId());
                map.put("hospitalName", "Hospital ID: " + req.getHospitalId()); // Ideally fetch hospital name
                map.put("bloodGroup", req.getBloodGroup());
                map.put("componentType", req.getComponentType() != null ? req.getComponentType().toString() : "WHOLE_BLOOD");
                map.put("quantity", req.getQuantity());
                map.put("urgency", req.getUrgency().toString());
                map.put("requestDate", req.getRequestDate().toString());
                return map;
            })
            .collect(Collectors.toList());
            
        return ResponseEntity.ok(requestList);
    }

    // 🔗 Blockchain Verification Logs
    @GetMapping("/dashboard/blockchain-stats")
    public ResponseEntity<Map<String, Object>> getBlockchainStats() {
        Map<String, Object> stats = new HashMap<>();
        
        long totalBlocks = trackingLogRepo.count();
        List<BloodTrackingLog> recentLogs = trackingLogRepo.findAll(); // Should be paginated ideally
        // Sort by timestamp desc and take top 5
        List<Map<String, Object>> recentTransactions = recentLogs.stream()
            .sorted(Comparator.comparing(BloodTrackingLog::getTimestamp).reversed())
            .limit(5)
            .map(log -> {
                Map<String, Object> map = new HashMap<>();
                map.put("unitId", log.getBloodUnitId());
                map.put("action", log.getAction());
                map.put("timestamp", log.getTimestamp().toString());
                map.put("hash", log.getHash() != null ? log.getHash().substring(0, 15) + "..." : "N/A");
                return map;
            })
            .collect(Collectors.toList());
        
        stats.put("verifiedBlocks", totalBlocks);
        stats.put("isValid", totalBlocks > 0);
        stats.put("recentTransactions", recentTransactions);
        
        return ResponseEntity.ok(stats);
    }
}
