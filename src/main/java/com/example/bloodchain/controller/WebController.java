package com.example.bloodchain.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // 🏠 Home
    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/index.html")
    public String indexHtml() {
        return "index";
    }

    // 🔐 Auth Pages
    @GetMapping("/user_login.html")
    public String userLogin() {
        return "user_login";
    }

    @GetMapping("/user_signup.html")
    public String userSignup() {
        return "user_signup";
    }

    @GetMapping("/hospital_login.html")
    public String hospitalLogin() {
        return "hospital_login";
    }

    @GetMapping("/hospital_signup.html")
    public String hospitalSignup() {
        return "hospital_signup";
    }

    @GetMapping("/bloodbank_login.html")
    public String bloodBankLogin() {
        return "bloodbank_login";
    }

    @GetMapping("/bloodbank_signup.html")
    public String bloodBankSignup() {
        return "bloodbank_signup";
    }

    @GetMapping("/admin_login.html")
    public String adminLogin() {
        return "admin_login";
    }

    // 📊 Dashboards
    @GetMapping("/user_dashboard.html")
    public String userDashboard() {
        return "user_dashboard";
    }

    // 🩸 Donor Sub-pages
    @GetMapping("/user_profile.html")
    public String userProfile() {
        return "user_profile";
    }

    @GetMapping("/user_donate.html")
    public String userDonate() {
        return "user_donate";
    }

    @GetMapping("/user_blockchain.html")
    public String userBlockchain() {
        return "user_blockchain";
    }

    @GetMapping("/user_certificates.html")
    public String userCertificates() {
        return "user_certificates";
    }

    @GetMapping("/user_statistics.html")
    public String userStatistics() {
        return "user_statistics";
    }

    @GetMapping("/hospital_dashboard.html")
    public String hospitalDashboard() {
        return "hospital_dashboard";
    }

    // 🏥 Hospital Sub-pages
    @GetMapping("/hospital_donations.html")
    public String hospitalDonations() {
        return "hospital_donations";
    }

    @GetMapping("/hospital_approvals.html")
    public String hospitalApprovals() {
        return "hospital_approvals";
    }

    @GetMapping("/hospital_requests.html")
    public String hospitalRequests() {
        return "hospital_requests";
    }

    @GetMapping("/hospital_blockchain.html")
    public String hospitalBlockchain() {
        return "hospital_blockchain";
    }

    @GetMapping("/hospital_inventory.html")
    public String hospitalInventory() {
        return "hospital_inventory";
    }

    @GetMapping("/bloodbank_dashboard.html")
    public String bloodBankDashboard() {
        return "bloodbank_dashboard";
    }

    // 🩸 Blood Bank Sub-pages
    @GetMapping("/bloodbank_inventory.html")
    public String bloodBankInventory() {
        return "bloodbank_inventory";
    }

    @GetMapping("/bloodbank_donations.html")
    public String bloodBankDonations() {
        return "bloodbank_donations";
    }

    @GetMapping("/bloodbank_dispatch.html")
    public String bloodBankDispatch() {
        return "bloodbank_dispatch";
    }

    @GetMapping("/bloodbank_blockchain.html")
    public String bloodBankBlockchain() {
        return "bloodbank_blockchain";
    }

    @GetMapping("/admin_dashboard.html")
    public String adminDashboard() {
        return "admin_dashboard";
    }

    // 🏥 Profile Pages
    @GetMapping("/bloodbank_profile.html")
    public String bloodBankProfile() {
        return "bloodbank_profile";
    }
}
