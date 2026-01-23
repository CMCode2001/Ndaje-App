package com.ndajee.userservice.config;

import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) {
        log.info("Verification et initialisation du compte admin...");
        
        try {
            UserRegistrationRequest adminRequest = new UserRegistrationRequest();
            adminRequest.setPrenom("Super");
            adminRequest.setNom("Admin");
            adminRequest.setEmail("admin@ndaje-app.sn");
            adminRequest.setPassword("admin123");
            adminRequest.setTelephone("770000000");

            userService.registerAdmin(adminRequest);
            log.info("Initialisation du compte admin reussie.");
        } catch (Exception e) {
            log.warn("L'administrateur système existe probablement déjà ou Keycloak est indisponible : {}", e.getMessage());
        }
    }
}
