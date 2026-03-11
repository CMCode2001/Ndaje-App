package com.ndajee.userservice.service;

import com.ndajee.userservice.dto.*;
import com.ndajee.userservice.entities.Admin;
import com.ndajee.userservice.entities.Caravannier;
import com.ndajee.userservice.entities.Conducteur;
import com.ndajee.userservice.entities.Passager;
import com.ndajee.userservice.entities.Utilisateur;
import com.ndajee.userservice.exception.BusinessException;
import com.ndajee.userservice.repositories.AdminRepository;
import com.ndajee.userservice.repositories.CaravannierRepository;
import com.ndajee.userservice.repositories.ConducteurRepository;
import com.ndajee.userservice.repositories.PassagerRepository;
import com.ndajee.userservice.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PassagerRepository passagerRepository;
    @Mock
    private ConducteurRepository conducteurRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private AdminRepository adminRepository;
    @Mock
    private CaravannierRepository caravannierRepository;
    @Mock
    private KeycloakService keycloakService;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest registrationRequest;
    private Passager passager;

    @BeforeEach
    void setUp() {
        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setPrenom("John");
        registrationRequest.setNom("Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setTelephone("123456789");
        registrationRequest.setPassword("password");

        passager = new Passager();
        passager.setId("kc-id-123");
        passager.setPrenom("John");
        passager.setNom("Doe");
        passager.setEmail("john.doe@example.com");
        passager.setTelephone("123456789");
        passager.setRole("PASSAGER");
        passager.setActif(true);
    }

    @Test
    void registerPassager_ShouldSuccess() {
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(keycloakService.createUser(any(UserRegistrationRequest.class), eq("PASSAGER"))).thenReturn("kc-id-123");
        when(passagerRepository.save(any(Passager.class))).thenReturn(passager);

        UserResponse response = userService.registerPassager(registrationRequest);

        assertNotNull(response);
        assertEquals("kc-id-123", response.getId());
        assertEquals("PASSAGER", response.getRole());
        verify(passagerRepository, times(1)).save(any(Passager.class));
    }

    @Test
    void registerPassager_ShouldThrowException_WhenEmailExists() {
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.of(passager));

        assertThrows(BusinessException.class, () -> userService.registerPassager(registrationRequest));
        verify(keycloakService, never()).createUser(any(), any());
    }

    @Test
    void registerConducteur_ShouldSuccess() {
        Conducteur conducteur = new Conducteur();
        conducteur.setId("kc-id-123");
        conducteur.setRole("DRIVER");

        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(keycloakService.createUser(any(UserRegistrationRequest.class), eq("DRIVER"))).thenReturn("kc-id-123");
        when(conducteurRepository.save(any(Conducteur.class))).thenReturn(conducteur);

        UserResponse response = userService.registerConducteur(registrationRequest);

        assertNotNull(response);
        assertEquals("DRIVER", response.getRole());
    }

    @Test
    void registerAdmin_ShouldSuccess() {
        Admin admin = new Admin();
        admin.setId("kc-id-123");
        admin.setRole("ADMIN");

        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(keycloakService.createUser(any(UserRegistrationRequest.class), eq("ADMIN"))).thenReturn("kc-id-123");
        when(adminRepository.save(any(Admin.class))).thenReturn(admin);

        UserResponse response = userService.registerAdmin(registrationRequest);

        assertNotNull(response);
        assertEquals("ADMIN", response.getRole());
    }

    @Test
    void registerCaravannier_ShouldSuccess() {
        Caravannier caravannier = new Caravannier();
        caravannier.setId("kc-id-123");
        caravannier.setRole("CARAVANNIER");

        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(keycloakService.createUser(any(UserRegistrationRequest.class), eq("CARAVANNIER"))).thenReturn("kc-id-123");
        when(caravannierRepository.save(any(Caravannier.class))).thenReturn(caravannier);

        UserResponse response = userService.registerCaravannier(registrationRequest);

        assertNotNull(response);
        assertEquals("CARAVANNIER", response.getRole());
    }

    @Test
    void getUserById_ShouldReturnUser() {
        when(utilisateurRepository.findById("kc-id-123")).thenReturn(Optional.of(passager));

        UserResponse response = userService.getUserById("kc-id-123");

        assertNotNull(response);
        assertEquals("kc-id-123", response.getId());
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        when(utilisateurRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userService.getUserById("not-found"));
    }

    @Test
    void login_ShouldSuccess_WhenUserExistsAndActive() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password");
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test-token");

        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.of(passager));
        when(keycloakService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        TokenResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getAccessToken());
    }

    @Test
    void login_ShouldThrowException_WhenUserInactive() {
        passager.setActif(false);
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password");

        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.of(passager));

        assertThrows(BusinessException.class, () -> userService.login(loginRequest));
        verify(keycloakService, never()).login(any());
    }

    @Test
    void updateProfile_ShouldSuccess() {
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setPrenom("Jane");

        when(utilisateurRepository.findById("kc-id-123")).thenReturn(Optional.of(passager));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(passager);
        doNothing().when(keycloakService).updateUser(eq("kc-id-123"), any(UpdateProfileRequest.class));

        UserResponse response = userService.updateProfile("kc-id-123", updateRequest);

        assertNotNull(response);
        verify(utilisateurRepository, times(1)).save(any(Utilisateur.class));
        verify(keycloakService, times(1)).updateUser(anyString(), any(UpdateProfileRequest.class));
    }
}
