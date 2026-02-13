package com.example.bloodchain.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/blockchain")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class BlockchainController {

    @GetMapping("/status")
    public Map<String, Object> getBlockchainStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("valid", true);
        response.put("message", "✅ Blockchain is valid and up-to-date.");
        return response;
    }
}
