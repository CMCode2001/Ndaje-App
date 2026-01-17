package com.ndajee.userservice.service;

import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.entities.Conducteur;
import com.ndajee.userservice.entities.Passager;
import com.ndajee.userservice.entities.Utilisateur;
import com.ndajee.userservice.enums.StatutConducteur;
import com.ndajee.userservice.exception.BusinessException;
import com.ndajee.userservice.repositories.ConducteurRepository;
import com.ndajee.userservice.repositories.PassagerRepository;
import com.ndajee.userservice.repositories.UtilisateurRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.ndajee.userservice.dto.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PassagerRepository passagerRepository;
    private final ConducteurRepository conducteurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final KeycloakService keycloakService;

@Transactional
public UserResponse registerPassager(UserRegistrationRequest request) {

    if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new BusinessException("Email déjà utilisé.");
    }

    String keycloakId = null;

    try {
        keycloakId = keycloakService.createUser(request, "PASSAGER");

        Passager passager = new Passager();
        mapCommonFields(passager, request);

        passager.setId(keycloakId);

        passager.setPointsFidelite(50);

        Passager saved = passagerRepository.save(passager);

        return mapToResponse(saved, "PASSAGER");

    } catch (Exception ex) {

        if (keycloakId != null) {
            try {
                keycloakService.deleteUser(keycloakId);
            } catch (Exception kcEx) {
                // log critique mais on ne masque pas l'erreur principale
                log.error("Échec rollback Keycloak pour l'utilisateur {}", keycloakId, kcEx);
            }
        }

        throw ex;
    }
}

    @Transactional
    public UserResponse registerConducteur(UserRegistrationRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email déjà utilisé localement.");
        }

        String keycloakId = null;

        try {
            keycloakId = keycloakService.createUser(request, "CONDUCTEUR");

            Conducteur conducteur = new Conducteur();
            mapCommonFields(conducteur, request);
            conducteur.setId(keycloakId);
            conducteur.setStatut(StatutConducteur.HORS_LIGNE); // Default status
            
            Conducteur saved = conducteurRepository.save(conducteur);
            return mapToResponse(saved, "CONDUCTEUR");
        } catch (Exception ex) {
            if (keycloakId != null) {
                try {
                    keycloakService.deleteUser(keycloakId);
                } catch (Exception kcEx) {
                    log.error("Échec rollback Keycloak pour l'utilisateur {}", keycloakId, kcEx);
                }
            }
            throw ex;
        }
    }
    // ...
    public TokenResponse login(LoginRequest request) {
        return keycloakService.login(request);
    }

    private void mapCommonFields(Utilisateur user, UserRegistrationRequest request) {
        user.setPrenom(request.getPrenom());
        user.setNom(request.getNom());
        user.setEmail(request.getEmail());
        user.setTelephone(request.getTelephone());
        user.setActif(true);
    }

    public void forgotPassword(String email) {
        keycloakService.forgotPassword(email);
    }

    @Transactional
    public UserResponse updateProfile(String id, UpdateProfileRequest request) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable."));

        user.setPrenom(request.getPrenom());
        user.setNom(request.getNom());
        user.setTelephone(request.getTelephone());
        
        Utilisateur saved = utilisateurRepository.save(user); // Sync local DB
        
        // Sync Keycloak
        keycloakService.updateUser(id, request);
        
        return mapToResponse(saved, getRoleFromEntity(saved));
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
