package com.example.bloodchain.controller;

import com.example.bloodchain.dto.EligibilityStatus;
import com.example.bloodchain.service.EligibilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/eligibility")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class EligibilityController {

    @Autowired
    private EligibilityService eligibilityService;

    @GetMapping("/check/{email}")
    public ResponseEntity<EligibilityStatus> checkEligibility(@PathVariable String email) {
        boolean eligible = eligibilityService.isEligible(email);
        String reason = eligibilityService.getIneligibilityReason(email);
        LocalDate nextDate = eligibilityService.getNextEligibleDate(email);
        
        return ResponseEntity.ok(new EligibilityStatus(eligible, reason, nextDate));
    }
}
