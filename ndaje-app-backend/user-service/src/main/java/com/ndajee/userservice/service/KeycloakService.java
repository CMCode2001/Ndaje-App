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
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import java.util.Collections;
import java.util.List;

/**
 * Service technique gérant l'intégration directe avec l'API Admin de Keycloak.
 */
@Service
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.server-url}")
    private String keycloakUrl;
    
    @Value("${keycloak.admin.client-id}")
    private String clientId;
    
    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public KeycloakService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Crée un utilisateur dans Keycloak avec un mot de passe et un rôle.
     * @return L'ID (UUID) généré par Keycloak
     */
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
    
    /**
     * Authentifie un utilisateur auprès de Keycloak (Direct Access Grant).
     * @return Les tokens JWT (Access, Refresh)
     */
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

    /** Met à jour le nom/prénom dans Keycloak */
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

    public void logout(String refreshToken) {
        try {
            String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/logout";
            
            String body = "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8) +
                          "&client_secret=" + URLEncoder.encode(clientSecret, StandardCharsets.UTF_8) +
                          "&refresh_token=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 204) {
                throw new BusinessException("Erreur lors de la déconnexion Keycloak: " + response.statusCode());
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("Erreur technique lors de la déconnexion : " + e.getMessage());
        }
    }
}
