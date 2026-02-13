package com.example.bloodchain.controller;

import com.example.bloodchain.model.*;
import com.example.bloodchain.repository.*;
import com.example.bloodchain.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class AdminController {

    @Autowired private DonorRepository donorRepo;
    @Autowired private HospitalRepository hospitalRepo;
    @Autowired private BloodBankRepository bloodBankRepo;
    @Autowired private EmailService emailService;

    // 🩸 Get all donors
    @GetMapping("/donors")
    public List<Donor> getAllDonors() {
        return donorRepo.findAll();
    }

    // 🏥 Get all hospitals
    @GetMapping("/hospitals")
    public List<Hospital> getAllHospitals() {
        return hospitalRepo.findAll();
    }

    // 🧫 Get all blood banks
    @GetMapping("/bloodbanks")
    public List<BloodBank> getAllBloodBanks() {
        return bloodBankRepo.findAll();
    }

    // ✉️ Send email to any user
    @PostMapping("/contact")
    public String sendEmail(@RequestBody EmailRequest emailRequest) {
        emailService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getBody());
        return "✅ Email sent successfully to " + emailRequest.getTo();
    }

    // 📄 Download CSV report (donors/hospitals/bloodbanks)
    @GetMapping("/reports/{type}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String type) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);

        switch (type.toLowerCase()) {
            case "donors" -> {
                writer.println("Name,Email,BloodGroup,City,LastDonation");
                for (Donor d : donorRepo.findAll()) {
                    writer.printf("%s,%s,%s,%s,%s%n", d.getName(), d.getEmail(), d.getBloodGroup(), d.getCity(), d.getLastDonation());
                }
            }
            case "hospitals" -> {
                writer.println("Name,Email,Contact,City,TotalPatients");
                for (Hospital h : hospitalRepo.findAll()) {
                    writer.printf("%s,%s,%s,%s,%d%n", h.getName(), h.getEmail(), h.getContactNumber(), h.getCity(), h.getTotalPatients());
                }
            }
            case "bloodbanks" -> {
                writer.println("Name,Email,City,Capacity,AvailableUnits");
                for (BloodBank b : bloodBankRepo.findAll()) {
                    writer.printf("%s,%s,%s,%d,%d%n", b.getName(), b.getEmail(), b.getCity(), b.getCapacity(), b.getAvailableUnits());
                }
            }
            default -> writer.println("Invalid report type!");
        }

        writer.flush();
        byte[] csvData = outputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(csvData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
