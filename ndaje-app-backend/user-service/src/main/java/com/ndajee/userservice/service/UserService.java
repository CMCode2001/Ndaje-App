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
import com.ndajee.userservice.entities.Admin;
import com.ndajee.userservice.repositories.AdminRepository;
import com.ndajee.userservice.dto.*;

/**
 * Service gérant la logique métier des utilisateurs.
 * Assure la cohérence entre la base de données locale et Keycloak.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final PassagerRepository passagerRepository;
    private final ConducteurRepository conducteurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AdminRepository adminRepository;
    private final KeycloakService keycloakService;


    /**
     * Inscrit un nouveau passager. Utilise une transaction pour assurer que 
     * l'utilisateur est supprimé de Keycloak si l'enregistrement local échoue (rollback).
     */
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

        passager.setRole("PASSAGER");
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

    /**
     * Inscrit un nouveau conducteur avec rollback automatique en cas d'erreur.
     */
    @Transactional
    public UserResponse registerConducteur(UserRegistrationRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException("Email déjà utilisé localement.");
        }

        String keycloakId = null;

        try {
            keycloakId = keycloakService.createUser(request, "DRIVER");

            Conducteur conducteur = new Conducteur();
            mapCommonFields(conducteur, request);
            conducteur.setId(keycloakId);
            conducteur.setRole("DRIVER");
            conducteur.setStatut(StatutConducteur.HORS_LIGNE); // Default status
            
            Conducteur saved = conducteurRepository.save(conducteur);
            return mapToResponse(saved, "DRIVER");
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

    /**
     * Inscrit un administrateur système.
     */
    @Transactional
    public UserResponse registerAdmin(UserRegistrationRequest request) {
        if (utilisateurRepository.findByEmail(request.getEmail()).isPresent()) {
            return mapToResponse(utilisateurRepository.findByEmail(request.getEmail()).get(), "ADMIN");
        }

        String keycloakId = null;

        try {
            keycloakId = keycloakService.createUser(request, "ADMIN");

            Admin admin = new Admin();
            mapCommonFields(admin, request);
            admin.setId(keycloakId);
            admin.setRole("ADMIN");

            Admin saved = adminRepository.save(admin);
            return mapToResponse(saved, "ADMIN");
        } catch (Exception ex) {
            if (keycloakId != null) {
                try {
                    keycloakService.deleteUser(keycloakId);
                } catch (Exception kcEx) {
                    log.error("Échec rollback Keycloak pour l'administrateur {}", keycloakId, kcEx);
                }
            }
            throw ex;
        }
    }
    /**
     * Récupère un utilisateur par son ID.
     */
    public UserResponse getUserById(String id) {
        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé."));
        return mapToResponse(user, getRoleFromEntity(user));
    }
    public TokenResponse login(LoginRequest request) {
        // Vérifier si l'utilisateur existe et est actif dans la base de données locale
        Utilisateur user = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Email ou mot de passe incorrect."));
        
        // Vérifier si le compte est actif
        if (!user.isActif()) {
            throw new BusinessException("Compte désactivé");
        }
        
        // Procéder à l'authentification Keycloak
        return keycloakService.login(request);
    }

    public void logout(LogoutRequest request) {
        keycloakService.logout(request.getRefreshToken());
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

    /**
     * Modifie les informations du profil. Synchronise les changements localement et sur Keycloak.
     */
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
        if (user.getRole() != null) return user.getRole();
        if (user instanceof Passager) return "PASSAGER";
        if (user instanceof Conducteur) return "DRIVER";
        if (user instanceof Admin) return "ADMIN";
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
