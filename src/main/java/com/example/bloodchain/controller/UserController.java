package com.example.bloodchain.controller;

import com.example.bloodchain.model.Donation;
import com.example.bloodchain.model.User;
import com.example.bloodchain.repository.UserRepository;
import com.example.bloodchain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.bloodchain.model.BloodUnit;
import com.example.bloodchain.model.BloodTrackingLog;
import com.example.bloodchain.model.Donor;
import com.example.bloodchain.repository.BloodUnitRepository;
import com.example.bloodchain.repository.BloodTrackingLogRepository;
import com.example.bloodchain.repository.DonorRepository;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepo;

    @Autowired
    private BloodUnitRepository bloodUnitRepo;

    @Autowired
    private BloodTrackingLogRepository logRepo;

    // ✅ Common Signup API
    @PostMapping("/signup")
    public String register(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // ✅ User Login
    @PostMapping("/login/user")
    public String loginUser(@RequestBody User user) {
        boolean valid = userService.validateLoginByRole(user.getEmail(), user.getPassword(), "user");
        return valid ? "User login successful!" : "Invalid user credentials!";
    }

    // ✅ Hospital Login
    @PostMapping("/login/hospital")
    public String loginHospital(@RequestBody User user) {
        boolean valid = userService.validateLoginByRole(user.getEmail(), user.getPassword(), "hospital");
        return valid ? "Hospital login successful!" : "Unauthorized! You are not a hospital user.";
    }

    // ✅ Blood Bank Login
    @PostMapping("/login/bloodbank")
    public String loginBloodBank(@RequestBody User user) {
        boolean valid = userService.validateLoginByRole(user.getEmail(), user.getPassword(), "bloodbank");
        return valid ? "Blood Bank login successful!" : "Unauthorized! You are not a blood bank user.";
    }

    // ✅ Admin Login
    @PostMapping("/login/admin")
    public String loginAdmin(@RequestBody User user) {
        boolean valid = userService.validateAdminLogin(user.getEmail(), user.getPassword());
        return valid ? "Admin login successful!" : "Unauthorized! Only the Super Admin can access this panel.";
    }

    // ✅ Get User Profile by Email
    @GetMapping("/user/{email}")
    public User getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email);
    }

    // ✅ Get Donation History for a User (Updated to fetch from BloodUnit and Blockchain)
    @GetMapping("/user/{email}/donations")
    public ResponseEntity<List<Map<String, Object>>> getUserDonations(@PathVariable String email) {
        Donor donor = donorRepo.findByEmail(email);
        if (donor == null) return ResponseEntity.ok(Collections.emptyList());
        
        List<BloodUnit> units = bloodUnitRepo.findByDonorId(donor.getDonorId());
        
        List<Map<String, Object>> records = units.stream().map(unit -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", unit.getBloodUnitId());
            map.put("bloodUnitId", unit.getBloodUnitId());
            map.put("units", unit.getVolumeMl());
            map.put("donationDate", unit.getCollectionDate().toString());
            map.put("location", unit.getStorageLocation() != null ? unit.getStorageLocation() : "Blood Bank");
            map.put("status", unit.getStatus().toString());
            
            // Fetch blockchain hash
            BloodTrackingLog log = logRepo.findTopByBloodUnitIdOrderByTimestampDesc(unit.getBloodUnitId());
            map.put("blockHash", log != null && log.getHash() != null ? log.getHash() : "Pending Verification");
            
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(records);
    }

    // ✅ Update user details
    @PutMapping("/user/update/{email}")
    public ResponseEntity<String> updateUser(@PathVariable String email, @RequestBody User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(email);

        if (existingUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User existingUser = existingUserOpt.get();

        // ✅ Update only non-null fields
        if (updatedUser.getName() != null) existingUser.setName(updatedUser.getName());
        if (updatedUser.getBloodGroup() != null) existingUser.setBloodGroup(updatedUser.getBloodGroup());
        if (updatedUser.getPhone() != null) existingUser.setPhone(updatedUser.getPhone());
        if (updatedUser.getCity() != null) existingUser.setCity(updatedUser.getCity());
        if (updatedUser.getRole() != null) existingUser.setRole(updatedUser.getRole());
        if (updatedUser.getStatus() != null) existingUser.setStatus(updatedUser.getStatus());
        if (updatedUser.getLastDonationDate() != null) existingUser.setLastDonationDate(updatedUser.getLastDonationDate());

        userRepository.save(existingUser);
        return ResponseEntity.ok("Profile updated successfully");
    }

    // ✅ Check Donor Eligibility
    @GetMapping("/user/eligibility/{email}")
    public ResponseEntity<java.util.Map<String, Object>> checkEligibility(@PathVariable String email) {
        Optional<User> userOpt = userRepository.findById(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        User user = userOpt.get();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        if (user.getLastDonationDate() == null) {
            response.put("eligible", true);
            response.put("reason", "No previous donations found. You are good to go!");
            response.put("nextEligibleDate", java.time.LocalDate.now().toString());
        } else {
            java.time.LocalDate lastDonation = java.time.LocalDate.parse(user.getLastDonationDate());
            java.time.LocalDate nextEligible = lastDonation.plusMonths(3);
            
            if (java.time.LocalDate.now().isAfter(nextEligible) || java.time.LocalDate.now().equals(nextEligible)) {
                response.put("eligible", true);
                response.put("reason", "You have completed the 3-month gap.");
                response.put("nextEligibleDate", java.time.LocalDate.now().toString());
            } else {
                response.put("eligible", false);
                response.put("reason", "Gap of 3 months required between donations.");
                response.put("nextEligibleDate", nextEligible.toString());
            }
        }
        
        return ResponseEntity.ok(response);
    }
}
