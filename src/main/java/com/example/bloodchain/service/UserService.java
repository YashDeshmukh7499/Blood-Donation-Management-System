package com.example.bloodchain.service;

import com.example.bloodchain.model.Donation;
import com.example.bloodchain.model.User;
import com.example.bloodchain.repository.DonationRepository;
import com.example.bloodchain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;  // ✅ Correct import for java.util.List

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private DonationRepository donationRepo;

    @Autowired
    private BCryptPasswordEncoder encoder;

    // ✅ Register new user (unique email per role)
    public String registerUser(User user) {
        // Check if user with same email and role already exists
        if (repo.existsByEmailAndRole(user.getEmail(), user.getRole())) {
            return "❌ Email already registered for this role!";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        repo.save(user);

        switch (user.getRole().toLowerCase()) {
            case "user":
                return "✅ User registered successfully!";
            case "hospital":
                return "✅ Hospital registered successfully!";
            case "bloodbank":
                return "✅ Blood Bank registered successfully!";
            case "admin":
                return "✅ Admin registered successfully!";
            default:
                return "✅ Registration successful!";
        }
    }

    // ✅ Validate login for each role
    public boolean validateLoginByRole(String email, String password, String role) {
        User user = repo.findByEmail(email);
        if (user == null) return false;
        if (!user.getRole().equalsIgnoreCase(role)) return false;
        return encoder.matches(password, user.getPassword());
    }

    // ✅ Validate Admin login (only super admin allowed)
    public boolean validateAdminLogin(String email, String password) {
        User user = repo.findByEmail(email);
        if (user == null) return false;

        // Only one true admin can log in
        boolean isAdmin = user.getRole().equalsIgnoreCase("admin")
                && user.getEmail().equalsIgnoreCase("admin@bloodchain.com");

        return isAdmin && encoder.matches(password, user.getPassword());
    }

    // ✅ Fetch single user details by email
    public User getUserByEmail(String email) {
        return repo.findByEmail(email);
    }

    // ✅ Fetch user donation history
    public List<Donation> getUserDonations(String email) {
        return donationRepo.findByEmail(email);
    }

}
