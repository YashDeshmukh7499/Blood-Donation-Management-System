package com.example.bloodchain.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = {"http://localhost:63342", "http://127.0.0.1:63342"})
public class InventoryController {

    @GetMapping("/hospital")
    public List<Map<String, Object>> getHospitalInventory() {
        List<Map<String, Object>> inventory = new ArrayList<>();

        inventory.add(Map.of("group", "A+", "units", 15));
        inventory.add(Map.of("group", "B+", "units", 8));
        inventory.add(Map.of("group", "O+", "units", 22));
        inventory.add(Map.of("group", "AB+", "units", 5));
        inventory.add(Map.of("group", "A-", "units", 3));
        inventory.add(Map.of("group", "O-", "units", 12));

        return inventory;
    }
}
