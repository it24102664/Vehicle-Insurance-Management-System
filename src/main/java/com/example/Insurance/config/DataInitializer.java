package com.example.Insurance.config;


import com.example.Insurance.model.Admin;
import com.example.Insurance.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private AdminService adminService;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin if none exists
        if (adminService.findByEmail("admin@motorcare.lk").isEmpty()) {
            Admin admin = new Admin();
            admin.setEmail("admin@motorcare.lk");
            admin.setPassword("admin123"); // This will be hashed by AdminService
            admin.setName("System Administrator");

            adminService.createAdmin(admin);
            System.out.println("Default admin created: admin@motorcare.lk / admin123");
        }
    }
}