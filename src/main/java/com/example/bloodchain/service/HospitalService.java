package com.example.bloodchain.service;

import com.example.bloodchain.model.Hospital;
import com.example.bloodchain.repository.HospitalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {

    @Autowired
    private HospitalRepository hospitalRepository;

    // ➕ Add or Update a Hospital
    public String saveHospital(Hospital hospital) {
        hospitalRepository.save(hospital);
        return "✅ Hospital saved successfully!";
    }

    // 📄 Get all Hospitals
    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    // 🔍 Get Hospital by ID
    public Optional<Hospital> getHospitalById(int id) {
        return hospitalRepository.findById(id);
    }

    // ❌ Delete a Hospital
    public String deleteHospital(int id) {
        if (hospitalRepository.existsById(id)) {
            hospitalRepository.deleteById(id);
            return "✅ Hospital deleted successfully.";
        } else {
            return "❌ Hospital not found.";
        }
    }

    // ✏️ Update Hospital Details
    public String updateHospital(int id, Hospital updatedHospital) {
        Optional<Hospital> existing = hospitalRepository.findById(id);
        if (existing.isPresent()) {
            Hospital h = existing.get();
            h.setName(updatedHospital.getName());
            h.setEmail(updatedHospital.getEmail());
            h.setContactNumber(updatedHospital.getContactNumber());
            h.setCity(updatedHospital.getCity());
            h.setTotalPatients(updatedHospital.getTotalPatients());
            hospitalRepository.save(h);
            return "✅ Hospital updated successfully.";
        } else {
            return "❌ Hospital not found.";
        }
    }
}
