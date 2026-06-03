package com.RuanPablo2.fleet_auth_service.config;

import com.RuanPablo2.fleet_auth_service.models.User;
import com.RuanPablo2.fleet_auth_service.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String demoEmail = "demo@fleetrisk.com";

            if (userRepository.findByEmail(demoEmail).isEmpty()) {
                User demoUser = new User();
                demoUser.setBrokerName("Fleet Risk Demonstração");
                demoUser.setCnpj("32.268.697/0001-72");
                demoUser.setEmail(demoEmail);
                demoUser.setPassword(passwordEncoder.encode("demo@1234"));

                userRepository.save(demoUser);
                System.out.println("✅ [SEEDER] Usuário Demo criado com sucesso!");
            }
        };
    }
}