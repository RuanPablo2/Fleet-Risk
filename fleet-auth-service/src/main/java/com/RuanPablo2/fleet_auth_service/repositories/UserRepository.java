package com.RuanPablo2.fleet_auth_service.repositories;

import com.RuanPablo2.fleet_auth_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}