package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.ndajee.userservice.dto.UpdateProfileRequest;
import com.ndajee.userservice.dto.LogoutRequest;

/**
 * Contrôleur gérant les opérations utilisateurs standards : inscription,
 * connexion, profil.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** Inscription d'un passager - Création Keycloak + BDD locale */
    @PostMapping("/register/passenger")
    public ResponseEntity<UserResponse> registerPassenger(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerPassager(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Inscription d'un conducteur - Création Keycloak + BDD locale */
    @PostMapping("/register/driver")
    public ResponseEntity<UserResponse> registerDriver(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerConducteur(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Inscription d'un caravannier - Création Keycloak + BDD locale */
    @PostMapping("/register/caravannier")
    public ResponseEntity<UserResponse> registerCaravannier(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse response = userService.registerCaravannier(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /** Authentification via Keycloak et retour du token JWT */
    @PostMapping("/login")
    public ResponseEntity<com.ndajee.userservice.dto.TokenResponse> login(
            @Valid @RequestBody com.ndajee.userservice.dto.LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    /** Déconnexion (invalidation refresh token) */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        userService.logout(request);
        return ResponseEntity.noContent().build();
    }

    /** Envoi d'un email de réinitialisation de mot de passe (via Keycloak) */
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        userService.forgotPassword(email);
        return ResponseEntity.noContent().build();
    }

    /** Récupère les informations de profil par ID */
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@org.springframework.web.bind.annotation.PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /** Mise à jour des informations de profil (Synchronisé Keycloak + BDD) */
    @org.springframework.web.bind.annotation.PutMapping("/{id}/profile")
    public ResponseEntity<UserResponse> updateProfile(@org.springframework.web.bind.annotation.PathVariable String id,
            @Valid @RequestBody com.ndajee.userservice.dto.UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(id, request);
        return ResponseEntity.ok(response);
    }
}
