package com.ndajee.userservice.web;

import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur réservé aux administrateurs pour la gestion globale des utilisateurs.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** Liste tous les utilisateurs enregistrés */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** Récupère les détails d'un utilisateur par son ID */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    /** Supprime un utilisateur définitivement (Keycloak + BDD) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /** Active ou désactive un utilisateur (Keycloak + BDD) */
    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable String id, @RequestParam boolean active) {
        adminService.updateUserStatus(id, active);
        return ResponseEntity.noContent().build();
    }
}
