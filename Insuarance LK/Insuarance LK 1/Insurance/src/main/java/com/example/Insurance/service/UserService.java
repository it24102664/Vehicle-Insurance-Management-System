package com.example.Insurance.service;

import com.example.Insurance.model.User;
import com.example.Insurance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User email already exists");
        }

        // Check if NIC already exists
        if (userRepository.existsByNic(user.getNic())) {
            throw new RuntimeException("User NIC already exists");
        }

        // Store password as plain text (no encoding)
        // user.setPassword(user.getPassword()); // Already set, no need to change

        // Set default role
        user.setRole(User.UserRole.USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByNic(String nic) {
        return userRepository.findByNic(nic);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public boolean authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Simple string comparison - NO passwordEncoder
            return user.isEnabled() && password.equals(user.getPassword());
        }
        return false;
    }

    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public void registerUser(User user) {
        createUser(user);
    }

    public boolean changePassword(String email, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check old password with simple string comparison
            if (user.getPassword().equals(oldPassword)) {
                user.setPassword(newPassword);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }
}
