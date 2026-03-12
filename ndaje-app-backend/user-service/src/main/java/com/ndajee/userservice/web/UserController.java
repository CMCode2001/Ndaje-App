package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.LogoutRequest;
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

        com.ndajee.userservice.dto.TokenResponse tokenResponse = userService.login(request);

        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie
                .from("accessToken", tokenResponse.getAccessToken())
                .httpOnly(true)
                .secure(false) // Mettre à true en production HTTPS
                .path("/")
                .maxAge(tokenResponse.getExpiresIn())
                .sameSite("Strict")
                .build();

        org.springframework.http.ResponseCookie refreshCookie = org.springframework.http.ResponseCookie
                .from("refreshToken", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(false) // Mettre à true en production HTTPS
                .path("/")
                .maxAge(Long.parseLong(tokenResponse.getRefreshExpiresIn()))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(tokenResponse);
    }

    /** Déconnexion (invalidation refresh token) */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            @org.springframework.web.bind.annotation.CookieValue(name = "refreshToken", required = false) String refreshTokenCookie) {

        String tokenToRevoke = (request != null && request.getRefreshToken() != null)
                ? request.getRefreshToken()
                : refreshTokenCookie;

        if (tokenToRevoke != null && !tokenToRevoke.isEmpty()) {
            LogoutRequest dummyRequest = new LogoutRequest();
            dummyRequest.setRefreshToken(tokenToRevoke);
            userService.logout(dummyRequest);
        }

        org.springframework.http.ResponseCookie jwtCookie = org.springframework.http.ResponseCookie
                .from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        org.springframework.http.ResponseCookie refreshCookie = org.springframework.http.ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .header(org.springframework.http.HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
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
