package com.ndajee.userservice.service;

import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.exception.BusinessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.NotAuthorizedException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.OAuth2Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.ndajee.userservice.dto.TokenResponse;
import com.ndajee.userservice.dto.LoginRequest;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.RoleRepresentation;

import java.util.Collections;
import java.util.List;

@Service
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    private final String keycloakUrl = "http://localhost:8081";
    private final String clientId = "ndajee-client";
    private final String clientSecret = "juYkaotpPqXyjpXC0NFttPMS7hYsiOPL";

    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public String createUser(UserRegistrationRequest userRegistrationRequest, String role) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(userRegistrationRequest.getEmail());
            user.setEmail(userRegistrationRequest.getEmail());
            user.setFirstName(userRegistrationRequest.getPrenom());
            user.setLastName(userRegistrationRequest.getNom());
            user.setEmailVerified(true);

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setValue(userRegistrationRequest.getPassword());
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);

            user.setCredentials(Collections.singletonList(credential));

            UsersResource usersResource = keycloak.realm(realm).users();
            Response response = usersResource.create(user);



            if (response.getStatus() == 201) {
                String userId = CreatedResponseUtil.getCreatedId(response);
                
                try {
                    // Assign Role
                    RoleRepresentation roleRep = keycloak.realm(realm).roles().get(role).toRepresentation();
                    usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(roleRep));
                } catch (Exception e) {
                    // Rollback: delete user if role assignment fails
                    usersResource.delete(userId);
                    throw new BusinessException("Erreur lors de l'assignation du rôle '" + role + "' : " + e.getMessage());
                }
                
                return userId;
            } else if (response.getStatus() == 409) {
                throw new BusinessException("L'utilisateur existe déjà dans Keycloak.");
            } else {
                throw new BusinessException("Erreur Keycloak (" + response.getStatus() + "): " + response.getStatusInfo());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur de communication avec Keycloak : " + e.getMessage());
        }
    }
    
    public TokenResponse login(LoginRequest request) {
        try {
            Keycloak keycloakUser = KeycloakBuilder.builder()
                    .serverUrl(keycloakUrl)
                    .realm(realm)
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId(clientId)
                    .clientSecret(clientSecret)
                    .username(request.getEmail())
                    .password(request.getPassword())
                    .build();

            AccessTokenResponse token = keycloakUser.tokenManager().getAccessToken();
            
            return new TokenResponse(
                    token.getToken(),
                    token.getRefreshToken(),
                    token.getExpiresIn(),
                    String.valueOf(token.getRefreshExpiresIn()),
                    token.getTokenType()
            );
        } catch (NotAuthorizedException e) {
             throw new BusinessException("Email ou mot de passe incorrect.");
        } catch (Exception e) {
             throw new BusinessException("Erreur d'authentification : " + e.getMessage());
        }
    }
    
    public void deleteUser(String userId) {
        try {
            keycloak.realm(realm).users().delete(userId);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la suppression de l'utilisateur Keycloak : " + e.getMessage());
        }
    }

    public void forgotPassword(String email) {
        try {
            List<UserRepresentation> users = keycloak.realm(realm).users().searchByEmail(email, true);
            if (users.isEmpty()) {
                throw new BusinessException("Aucun utilisateur trouvé avec cet email.");
            }
            UserRepresentation user = users.get(0);
            
            // Trigger Reset Password Email
            keycloak.realm(realm).users().get(user.getId())
                    .executeActionsEmail(Collections.singletonList("UPDATE_PASSWORD"));
                    
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de l'envoi de l'email de réinitialisation : " + e.getMessage());
        }
    }

    public void updateUser(String userId, com.ndajee.userservice.dto.UpdateProfileRequest request) {
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            user.setFirstName(request.getPrenom());
            user.setLastName(request.getNom());
            // Note: email changes require verification, skipping for now as per plan
            
            keycloak.realm(realm).users().get(userId).update(user);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la mise à jour Keycloak : " + e.getMessage());
        }
    }

    public void setUserEnabled(String userId, boolean enabled) {
        try {
            UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();
            user.setEnabled(enabled);
            keycloak.realm(realm).users().get(userId).update(user);
        } catch (Exception e) {
            throw new BusinessException("Erreur lors de la modification du statut utilisateur Keycloak : " + e.getMessage());
        }
    }

    // Helper class
    static class CreatedResponseUtil {
        public static String getCreatedId(Response response) {
            java.net.URI location = response.getLocation();
            if (location == null) {
                return null;
            }
            String path = location.getPath();
            return path.substring(path.lastIndexOf('/') + 1);
        }
    }
}
