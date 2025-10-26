package com.example.Insurance.repository;

import com.example.Insurance.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA will automatically implement these methods
    Optional<User> findByEmail(String email);

    Optional<User> findByNic(String nic);

    boolean existsByEmail(String email);

    boolean existsByNic(String nic);

    List<User> findByEnabled(boolean enabled);

    // findAll() is already provided by JpaRepository
    // save() is already provided by JpaRepository
}
