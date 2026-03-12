package com.ndajee.userservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndajee.userservice.dto.LoginRequest;
import com.ndajee.userservice.dto.LogoutRequest;
import com.ndajee.userservice.dto.TokenResponse;
import com.ndajee.userservice.dto.UpdateProfileRequest;
import com.ndajee.userservice.dto.UserRegistrationRequest;
import com.ndajee.userservice.dto.UserResponse;
import com.ndajee.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;
    private UserRegistrationRequest registrationRequest;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setPrenom("John");
        registrationRequest.setNom("Doe");
        registrationRequest.setEmail("john.doe@example.com");
        registrationRequest.setTelephone("123456789");
        registrationRequest.setPassword("password");

        userResponse = new UserResponse();
        userResponse.setId("kc-id-123");
        userResponse.setPrenom("John");
        userResponse.setNom("Doe");
        userResponse.setEmail("john.doe@example.com");
        userResponse.setRole("PASSAGER");
    }

    @Test
    void registerPassenger_ShouldReturnCreated() throws Exception {
        when(userService.registerPassager(any(UserRegistrationRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("kc-id-123"))
                .andExpect(jsonPath("$.role").value("PASSAGER"));

        verify(userService, times(1)).registerPassager(any(UserRegistrationRequest.class));
    }

    @Test
    void registerDriver_ShouldReturnCreated() throws Exception {
        userResponse.setRole("DRIVER");
        when(userService.registerConducteur(any(UserRegistrationRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register/driver")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("kc-id-123"))
                .andExpect(jsonPath("$.role").value("DRIVER"));

        verify(userService, times(1)).registerConducteur(any(UserRegistrationRequest.class));
    }

    @Test
    void registerCaravannier_ShouldReturnCreated() throws Exception {
        userResponse.setRole("CARAVANNIER");
        when(userService.registerCaravannier(any(UserRegistrationRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/users/register/caravannier")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CARAVANNIER"));

        verify(userService, times(1)).registerCaravannier(any(UserRegistrationRequest.class));
    }

    @Test
    void login_ShouldReturnToken() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("john.doe@example.com");
        loginRequest.setPassword("password");
        TokenResponse tokenResponse = new TokenResponse();
        tokenResponse.setAccessToken("test-token");
        tokenResponse.setRefreshToken("mock-refresh-token");
        tokenResponse.setExpiresIn(3600L);
        tokenResponse.setRefreshExpiresIn("7200");

        when(userService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("test-token"));
    }

    @Test
    void forgotPassword_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).forgotPassword("john@example.com");

        mockMvc.perform(post("/api/users/forgot-password")
                .param("email", "john@example.com"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).forgotPassword("john@example.com");
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        when(userService.getUserById("kc-id-123")).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/kc-id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("kc-id-123"));
    }

    @Test
    void updateProfile_ShouldReturnUpdatedUser() throws Exception {
        UpdateProfileRequest updateRequest = new UpdateProfileRequest();
        updateRequest.setPrenom("Jane");
        updateRequest.setNom("Doe");

        userResponse.setPrenom("Jane");

        when(userService.updateProfile(eq("kc-id-123"), any(UpdateProfileRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put("/api/users/kc-id-123/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prenom").value("Jane"));
    }
}
