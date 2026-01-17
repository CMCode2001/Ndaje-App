package com.ndajee.userservice.service;

import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.entities.Conducteur;
import com.ndajee.userservice.entities.Passager;
import com.ndajee.userservice.entities.Utilisateur;
import com.ndajee.userservice.exception.BusinessException;
import com.ndajee.userservice.repositories.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakService keycloakService;

    public List<UserResponse> getAllUsers() {
        return utilisateurRepository.findAll().stream()
                .map(user -> mapToResponse(user, getRoleFromEntity(user)))
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé."));
        return mapToResponse(user, getRoleFromEntity(user));
    }

    @Transactional
    public void deleteUser(String id) {
        if (!utilisateurRepository.existsById(id)) {
            throw new BusinessException("Utilisateur non trouvé.");
        }
        // Local delete
        utilisateurRepository.deleteById(id);
        // Keycloak delete
        keycloakService.deleteUser(id);
    }

    @Transactional
    public void updateUserStatus(String id, boolean active) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé."));
        
        user.setActif(active);
        utilisateurRepository.save(user); // Sync local
        
        // Sync Keycloak
        keycloakService.setUserEnabled(id, active);
    }

    private String getRoleFromEntity(Utilisateur user) {
        if (user instanceof Passager) return "PASSAGER";
        if (user instanceof Conducteur) return "CONDUCTEUR";
        return "INCONNU";
    }

    private UserResponse mapToResponse(Utilisateur user, String role) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setPrenom(user.getPrenom());
        response.setNom(user.getNom());
        response.setEmail(user.getEmail());
        response.setTelephone(user.getTelephone());
        response.setRole(role);
        return response;
    }
}
