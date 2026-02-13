package com.example.bloodchain.controller;

import com.example.bloodchain.model.Donation;
import com.example.bloodchain.service.DonationService;
import com.example.bloodchain.repository.DonationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class DonationController {

    @Autowired
    private DonationService donationService;

    @Autowired
    private com.example.bloodchain.service.CertificateService certificateService;

    @Autowired
    private DonationRepository donationRepo; 

    @Autowired
    private com.example.bloodchain.repository.DonationRequestRepository donationRequestRepo;

    // ➕ Submit Donation Request (Donor Intent)
    @PostMapping("/request")
    public ResponseEntity<String> requestDonation(@RequestBody com.example.bloodchain.model.DonationRequest request) {
        request.setStatus(com.example.bloodchain.model.DonationRequestStatus.PENDING);
        donationRequestRepo.save(request);
        return ResponseEntity.ok("✅ Donation request submitted successfully! Waiting for approval.");
    }

    // 📄 All donations (for admin/hospital)
    @GetMapping("/all")
    public List<com.example.bloodchain.dto.DonationDTO> getAll() {
        return donationService.getAllDTO();
    }

    // 🕓 Pending donations (for hospital approval)
    @GetMapping("/pending")
    public List<com.example.bloodchain.dto.DonationDTO> getPendingDonations() {
        return donationService.getPendingDonationsDTO();
    }

    // ✅ Approve donation
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approveDonation(@PathVariable Long id) {
        Donation donation = donationRepo.findById(id).orElse(null);
        if (donation == null)
            return ResponseEntity.status(404).body("❌ Donation not found");

        donation.setStatus("Verified");
        donation.setBlockHash(UUID.randomUUID().toString().substring(0, 15)); // Mock blockchain hash
        donationRepo.save(donation);

        return ResponseEntity.ok("✅ Donation approved successfully!");
    }

    // ❌ Reject donation
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> rejectDonation(@PathVariable Long id) {
        Donation donation = donationRepo.findById(id).orElse(null);
        if (donation == null)
            return ResponseEntity.status(404).body("❌ Donation not found");

        donation.setStatus("Rejected");
        donationRepo.save(donation);

        return ResponseEntity.ok("🚫 Donation rejected successfully!");
    }

    // 👤 User’s donation history (for user dashboard)
    @GetMapping("/user/{email}")
    public List<Donation> getUserDonations(@PathVariable String email) {
        return donationService.getDonationsByEmail(email);
    }

    // 📜 Download Certificate
    @GetMapping("/{id}/certificate")
    public ResponseEntity<byte[]> getCertificate(@PathVariable String id) {
        try {
            byte[] pdf = certificateService.generateCertificateFromUnit(id);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=Donation_Certificate_" + id + ".pdf")
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
