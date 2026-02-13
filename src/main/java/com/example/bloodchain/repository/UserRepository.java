package com.example.bloodchain.repository;

import com.example.bloodchain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    boolean existsByEmailAndRole(String email, String role);
}
