package com.example.bloodchain;

import com.example.bloodchain.model.User;
import com.example.bloodchain.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BloodChainApplication {

    public static void main(String[] args) {
        SpringApplication.run(BloodChainApplication.class, args);
    }

    // ✅ Create a default admin if not exists
    @Bean
    public CommandLineRunner createAdmin(UserRepository repo, BCryptPasswordEncoder encoder) {
        return args -> {
            String adminEmail = "admin@bloodchain.com";
            if (!repo.existsById(adminEmail)) {
                User admin = new User();
                admin.setEmail(adminEmail);
                admin.setName("Super Admin");
                admin.setPassword(encoder.encode("Admin@123"));
                admin.setRole("admin");
                repo.save(admin);
                System.out.println("✅ Default admin created: " + adminEmail);
            }
        };
    }

}
