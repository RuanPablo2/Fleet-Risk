package com.RuanPablo2.fleet_auth_service.services;

import com.RuanPablo2.fleet_auth_service.models.User;
import com.RuanPablo2.fleet_auth_service.repositories.UserRepository;
import com.RuanPablo2.fleet_auth_service.security.JwtUtil;
import com.ruanpablo2.fleet_common.exceptions.BusinessRuleException;
import com.ruanpablo2.fleet_common.exceptions.UnauthorizedAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new BusinessRuleException("This e-mail is already registered.", "AUTH_422");
        }

        if (userRepository.findByCnpj(user.getCnpj()).isPresent()) {
            throw new BusinessRuleException("This CNPJ is already registered.", "AUTH_422");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        try {
            var usernamePassword = new UsernamePasswordAuthenticationToken(email, password);
            Authentication auth = authenticationManager.authenticate(usernamePassword);

            User user = (User) auth.getPrincipal();

            return jwtUtil.generateToken(user.getBrokerName(), user.getCnpj());

        } catch (BadCredentialsException e) {
            throw new UnauthorizedAccessException("Invalid e-mail or password.", "AUTH_401");
        }
    }
}