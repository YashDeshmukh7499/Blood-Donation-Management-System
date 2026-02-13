package com.example.bloodchain.service;

import com.example.bloodchain.model.BloodBank;
import com.example.bloodchain.repository.BloodBankRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BloodBankService {

    @Autowired
    private BloodBankRepository bloodBankRepository;

    // ➕ Add or Update a Blood Bank
    public String saveBloodBank(BloodBank bloodBank) {
        bloodBankRepository.save(bloodBank);
        return "✅ Blood Bank saved successfully!";
    }

    // 📄 Get all Blood Banks
    public List<BloodBank> getAllBloodBanks() {
        return bloodBankRepository.findAll();
    }

    // 🔍 Get Blood Bank by ID
    public Optional<BloodBank> getBloodBankById(int id) {
        return bloodBankRepository.findById(id);
    }

    // ❌ Delete Blood Bank
    public String deleteBloodBank(int id) {
        if (bloodBankRepository.existsById(id)) {
            bloodBankRepository.deleteById(id);
            return "✅ Blood Bank deleted successfully.";
        } else {
            return "❌ Blood Bank not found.";
        }
    }

    // ✏️ Update Blood Bank Details
    public String updateBloodBank(int id, BloodBank updatedBank) {
        Optional<BloodBank> existing = bloodBankRepository.findById(id);
        if (existing.isPresent()) {
            BloodBank bank = existing.get();
            bank.setName(updatedBank.getName());
            bank.setEmail(updatedBank.getEmail());
            bank.setCity(updatedBank.getCity());
            bank.setCapacity(updatedBank.getCapacity());
            bank.setAvailableUnits(updatedBank.getAvailableUnits());
            bloodBankRepository.save(bank);
            return "✅ Blood Bank updated successfully.";
        } else {
            return "❌ Blood Bank not found.";
        }
    }
}
