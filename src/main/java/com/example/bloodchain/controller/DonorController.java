package com.example.bloodchain.controller;

import com.example.bloodchain.model.Donor;
import com.example.bloodchain.repository.DonorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donor")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class DonorController {

    @Autowired
    private DonorRepository donorRepo;

    // ➕ Add a new donor
    @PostMapping("/add")
    public String addDonor(@RequestBody Donor donor) {
        donorRepo.save(donor);
        return "✅ Donor added successfully!";
    }

    // 📄 Get all donors
    @GetMapping("/all")
    public List<Donor> getAllDonors() {
        return donorRepo.findAll();
    }

    // ❌ Delete donor
    @DeleteMapping("/delete/{id}")
    public String deleteDonor(@PathVariable int id) {
        donorRepo.deleteById((long) id);
        return "✅ Donor deleted successfully.";
    }
}
