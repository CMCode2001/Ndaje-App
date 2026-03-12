package com.ndajee.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.dto.LoginRequest;
import com.ndajee.userservice.dto.TokenResponse;
import com.ndajee.userservice.repositories.UtilisateurRepository;
import com.ndajee.userservice.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration du UserController.
 *
 * Stratégie :
 * - @SpringBootTest démarre le contexte complet de l'application.
 * - Une base H2 en mémoire est utilisée à la place de PostgreSQL (profil
 * "integration").
 * - KeycloakService est mocké car il appelle un serveur externe.
 * - La sécurité OAuth2 est désactivée via le profil de test.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@DisplayName("UserController - Tests d'intégration")
class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UtilisateurRepository utilisateurRepository;

        @MockBean
        private KeycloakService keycloakService;

        @MockBean
        private com.ndajee.userservice.storage.S3StorageService s3StorageService;

        @BeforeEach
        void setUp() {
                utilisateurRepository.deleteAll();
        }

        // =========================================================================
        // Tests d'inscription
        // =========================================================================

        @Test
        @DisplayName("POST /api/users/register/passenger - Inscription passager réussie")
        void givenValidPassengerRequest_whenRegisterPassenger_thenReturns201WithUserResponse() throws Exception {
                // Arrange
                when(keycloakService.createUser(any(UserRegistrationRequest.class), anyString()))
                                .thenReturn("fake-userId");
                UserRegistrationRequest request = buildRegistrationRequest("John", "Doe", "john.doe@test.com",
                                "password123",
                                "+22790123456");

                // Act & Assert
                mockMvc.perform(post("/api/users/register/passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email", is("john.doe@test.com")))
                                .andExpect(jsonPath("$.prenom", is("John")))
                                .andExpect(jsonPath("$.nom", is("Doe")));
        }

        @Test
        @DisplayName("POST /api/users/register/passenger - Email invalide retourne 400")
        void givenInvalidEmail_whenRegisterPassenger_thenReturns400() throws Exception {
                // Arrange
                UserRegistrationRequest request = buildRegistrationRequest("Jane", "Doe", "not-an-email", "password123",
                                "+22790123456");

                // Act & Assert
                mockMvc.perform(post("/api/users/register/passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/users/register/passenger - Mot de passe trop court retourne 400")
        void givenShortPassword_whenRegisterPassenger_thenReturns400() throws Exception {
                // Arrange
                UserRegistrationRequest request = buildRegistrationRequest("Jane", "Doe", "jane@test.com", "123",
                                "+22790123456");

                // Act & Assert
                mockMvc.perform(post("/api/users/register/passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/users/register/driver - Inscription conducteur réussie")
        void givenValidDriverRequest_whenRegisterDriver_thenReturns201() throws Exception {
                // Arrange
                when(keycloakService.createUser(any(UserRegistrationRequest.class), anyString()))
                                .thenReturn("fake-userId");
                UserRegistrationRequest request = buildRegistrationRequest("Alice", "Martin", "alice.martin@test.com",
                                "securePass1", "+22791234567");

                // Act & Assert
                mockMvc.perform(post("/api/users/register/driver")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.email", is("alice.martin@test.com")));
        }

        @Test
        @DisplayName("POST /api/users/register/caravannier - Inscription caravannier réussie")
        void givenValidCaravannierRequest_whenRegisterCaravannier_thenReturns201() throws Exception {
                // Arrange
                when(keycloakService.createUser(any(UserRegistrationRequest.class), anyString()))
                                .thenReturn("fake-userId");
                UserRegistrationRequest request = buildRegistrationRequest("Bob", "Smith", "bob.smith@test.com",
                                "securePass2",
                                "+22792345678");

                // Act & Assert
                mockMvc.perform(post("/api/users/register/caravannier")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /api/users/register/passenger - Corps vide retourne 400")
        void givenEmptyBody_whenRegisterPassenger_thenReturns400() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/users/register/passenger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest());
        }

        // =========================================================================
        // Tests de connexion (login)
        // =========================================================================

        @Test
        @DisplayName("POST /api/users/login - Connexion réussie retourne les cookies et le token")
        void givenValidCredentials_whenLogin_thenReturns200WithCookies() throws Exception {
                // Arrange: Créer l'utilisateur en local (exigé par UserService.login)
                com.ndajee.userservice.entities.Passager localUser = new com.ndajee.userservice.entities.Passager();
                localUser.setEmail("user@test.com");
                localUser.setPrenom("Test");
                localUser.setNom("User");
                localUser.setActif(true);
                localUser.setId("fake-userId");
                utilisateurRepository.save(localUser);

                // Arrange: Mocker le service Keycloak pour retourner un faux TokenResponse
                TokenResponse fakeToken = new TokenResponse();
                fakeToken.setAccessToken("fake-jwt-access-token");
                fakeToken.setRefreshToken("fake-jwt-refresh-token");
                fakeToken.setExpiresIn(300L);
                fakeToken.setRefreshExpiresIn("1800");
                fakeToken.setTokenType("Bearer");
                when(keycloakService.login(any())).thenReturn(fakeToken);

                LoginRequest request = new LoginRequest();
                request.setEmail("user@test.com");
                request.setPassword("password123");

                // Act & Assert
                mockMvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(header().exists("Set-Cookie"))
                                .andExpect(jsonPath("$.accessToken", is("fake-jwt-access-token")));
        }

        @Test
        @DisplayName("POST /api/users/login - Champs obligatoires manquants retourne 400")
        void givenMissingCredentials_whenLogin_thenReturns400() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"email\": \"\", \"password\": \"\"}"))
                                .andExpect(status().isBadRequest());
        }

        // =========================================================================
        // Tests de déconnexion (logout)
        // =========================================================================

        @Test
        @DisplayName("POST /api/users/logout - Déconnexion réussie (via cookie) vide les cookies et retourne 204")
        void givenValidRefreshCookie_whenLogout_thenReturns204WithEmptyCookies() throws Exception {
                // Arrange
                doNothing().when(keycloakService).logout(anyString());

                // Act & Assert
                mockMvc.perform(post("/api/users/logout")
                                .cookie(new jakarta.servlet.http.Cookie("refreshToken", "valid-refresh-token")))
                                .andExpect(status().isNoContent())
                                .andExpect(header().exists("Set-Cookie"));
        }

        // =========================================================================
        // Tests de récupération de profil
        // =========================================================================

        @Test
        @DisplayName("GET /api/users/{id} - Profil non trouvé retourne 4xx")
        void givenNonExistentUserId_whenGetUserById_thenReturns4xx() throws Exception {
                // Act & Assert
                mockMvc.perform(get("/api/users/non-existent-id-999"))
                                .andExpect(status().is4xxClientError());
        }

        // =========================================================================
        // Tests du mot de passe oublié
        // =========================================================================

        @Test
        @DisplayName("POST /api/users/forgot-password - Envoie email de réinitialisation")
        void givenValidEmail_whenForgotPassword_thenReturns204() throws Exception {
                // Arrange
                doNothing().when(keycloakService).forgotPassword(anyString());

                // Act & Assert
                mockMvc.perform(post("/api/users/forgot-password")
                                .param("email", "user@test.com"))
                                .andExpect(status().isNoContent());
        }

        // =========================================================================
        // Méthodes utilitaires
        // =========================================================================

        private UserRegistrationRequest buildRegistrationRequest(String prenom, String nom, String email,
                        String password,
                        String telephone) {
                UserRegistrationRequest req = new UserRegistrationRequest();
                req.setPrenom(prenom);
                req.setNom(nom);
                req.setEmail(email);
                req.setPassword(password);
                req.setTelephone(telephone);
                return req;
        }
}
