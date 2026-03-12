package com.ndajee.carservice.service.impl;

import com.ndajee.carservice.domain.StatutVerificationVehicule;
import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import com.ndajee.carservice.exception.ResourceNotFoundException;
import com.ndajee.carservice.mapper.VehiculeMapper;
import com.ndajee.carservice.repository.VehiculeRepository;
import com.ndajee.carservice.service.VehiculeDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculeServiceImplTest {

    @Mock
    private VehiculeRepository vehiculeRepository;

    @Mock
    private VehiculeMapper vehiculeMapper;

    @Mock
    private VehiculeDocumentService vehiculeDocumentService;

    @InjectMocks
    private VehiculeServiceImpl vehiculeService;

    private Vehicule vehicule;
    private VehiculeRequest vehiculeRequest;
    private VehiculeResponse vehiculeResponse;

    @BeforeEach
    void setUp() {
        vehiculeRequest = new VehiculeRequest();
        vehiculeRequest.setDriverId("driver-1");
        vehiculeRequest.setMarque("Toyota");
        vehiculeRequest.setModele("Corolla");
        vehiculeRequest.setAnnee(2020);
        vehiculeRequest.setImmatriculation("AA-123-BB");
        vehiculeRequest.setPlaces(4);

        vehicule = new Vehicule();
        vehicule.setId(1L);
        vehicule.setDriverId("driver-1");
        vehicule.setMarque("Toyota");
        vehicule.setModele("Corolla");
        vehicule.setAnnee(2020);
        vehicule.setImmatriculation("AA-123-BB");
        vehicule.setPlaces(4);
        vehicule.setStatutVerification(StatutVerificationVehicule.EN_ATTENTE);

        vehiculeResponse = new VehiculeResponse();
        vehiculeResponse.setId(1L);
        vehiculeResponse.setDriverId("driver-1");
        vehiculeResponse.setMarque("Toyota");
        vehiculeResponse.setModele("Corolla");
        vehiculeResponse.setImmatriculation("AA-123-BB");
        vehiculeResponse.setPlaces(4);
        vehiculeResponse.setStatutVerification(StatutVerificationVehicule.EN_ATTENTE);
    }

    private void setMockSecurityContext(String userId) {
        JwtAuthenticationToken mockAuth = mock(JwtAuthenticationToken.class);
        lenient().when(mockAuth.getName()).thenReturn(userId);
        SecurityContext mockContext = mock(SecurityContext.class);
        lenient().when(mockContext.getAuthentication()).thenReturn(mockAuth);
        SecurityContextHolder.setContext(mockContext);
    }

    @Test
    void createVehicule_WithDriverIdInRequest_ShouldSuccess() {
        when(vehiculeMapper.toEntity(vehiculeRequest)).thenReturn(vehicule);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehicule);
        when(vehiculeMapper.toResponse(vehicule)).thenReturn(vehiculeResponse);

        VehiculeResponse response = vehiculeService.createVehicule(vehiculeRequest);

        assertNotNull(response);
        assertEquals("driver-1", response.getDriverId());
        assertEquals(StatutVerificationVehicule.EN_ATTENTE, response.getStatutVerification());
        verify(vehiculeRepository, times(1)).save(vehicule);
    }

    @Test
    void createVehicule_WithoutDriverId_ShouldFallbackToSecurityContext() {
        vehicule.setDriverId(null);
        vehiculeRequest.setDriverId(null);

        when(vehiculeMapper.toEntity(vehiculeRequest)).thenReturn(vehicule);
        when(vehiculeRepository.save(any(Vehicule.class))).thenReturn(vehicule);
        when(vehiculeMapper.toResponse(vehicule)).thenReturn(vehiculeResponse);

        SecurityContext securityContext = mock(SecurityContext.class);
        JwtAuthenticationToken jwtToken = mock(JwtAuthenticationToken.class);
        when(jwtToken.getName()).thenReturn("token-driver");
        when(securityContext.getAuthentication()).thenReturn(jwtToken);
        SecurityContextHolder.setContext(securityContext);

        VehiculeResponse response = vehiculeService.createVehicule(vehiculeRequest);

        assertNotNull(response);
        assertEquals("token-driver", vehicule.getDriverId());
        verify(vehiculeRepository, times(1)).save(vehicule);

        SecurityContextHolder.clearContext();
    }

    @Test
    void createVehicule_WithoutDriverIdAndContext_ShouldThrowException() {
        vehicule.setDriverId(null);
        vehiculeRequest.setDriverId(null);

        when(vehiculeMapper.toEntity(vehiculeRequest)).thenReturn(vehicule);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(RuntimeException.class, () -> vehiculeService.createVehicule(vehiculeRequest));
        verify(vehiculeRepository, never()).save(any(Vehicule.class));

        SecurityContextHolder.clearContext();
    }

    @Test
    void updateVehicule_ShouldSuccess_WhenVehiculeExists() {
        setMockSecurityContext("driver-1");
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        doNothing().when(vehiculeMapper).updateEntityFromRequest(vehiculeRequest, vehicule);
        when(vehiculeRepository.save(vehicule)).thenReturn(vehicule);
        when(vehiculeMapper.toResponse(vehicule)).thenReturn(vehiculeResponse);

        VehiculeResponse response = vehiculeService.updateVehicule(1L, vehiculeRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(vehiculeRepository, times(1)).save(vehicule);
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateVehicule_ShouldThrowException_WhenVehiculeNotFound() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.updateVehicule(1L, vehiculeRequest));
        verify(vehiculeRepository, never()).save(any(Vehicule.class));
    }

    @Test
    void getAllVehicules_ShouldReturnList() {
        when(vehiculeRepository.findAll()).thenReturn(List.of(vehicule));
        when(vehiculeMapper.toResponseList(List.of(vehicule))).thenReturn(List.of(vehiculeResponse));

        List<VehiculeResponse> list = vehiculeService.getAllVehicules();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void getVehiculeById_ShouldReturnOptional() {
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        when(vehiculeMapper.toResponse(vehicule)).thenReturn(vehiculeResponse);

        Optional<VehiculeResponse> response = vehiculeService.getVehiculeById(1L);

        assertTrue(response.isPresent());
        assertEquals(1L, response.get().getId());
    }

    @Test
    void getVehiculesByDriverId_ShouldReturnList() {
        when(vehiculeRepository.findByDriverId("driver-1")).thenReturn(List.of(vehicule));
        when(vehiculeMapper.toResponseList(List.of(vehicule))).thenReturn(List.of(vehiculeResponse));

        List<VehiculeResponse> list = vehiculeService.getVehiculesByDriverId("driver-1");

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void deleteVehicule_ShouldDelete() {
        setMockSecurityContext("driver-1");
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));
        doNothing().when(vehiculeRepository).deleteById(1L);

        vehiculeService.deleteVehicule(1L);

        verify(vehiculeRepository, times(1)).deleteById(1L);
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadDocument_ShouldSuccess() {
        setMockSecurityContext("driver-1");
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.of(vehicule));

        MockMultipartFile file = new MockMultipartFile("file", "cert.pdf", "application/pdf", "data".getBytes());
        DocumentResponse docResponse = new DocumentResponse();
        docResponse.setId(10L);

        when(vehiculeDocumentService.uploadDocument(1L, file, "ASSURANCE", "12345", "2025-12-31"))
                .thenReturn(docResponse);

        DocumentResponse response = vehiculeService.uploadDocument(1L, file, "ASSURANCE", "12345", "2025-12-31");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenVehiculeNotFound() {
        setMockSecurityContext("driver-1");
        when(vehiculeRepository.findById(1L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "cert.pdf", "application/pdf", "data".getBytes());

        assertThrows(ResourceNotFoundException.class,
                () -> vehiculeService.uploadDocument(1L, file, "ASSURANCE", "12345", "2025-12-31"));
        SecurityContextHolder.clearContext();
    }

    @Test
    void getVehiculeDocuments_ShouldSuccess() {
        when(vehiculeRepository.existsById(1L)).thenReturn(true);
        DocumentResponse docResponse = new DocumentResponse();
        when(vehiculeDocumentService.getDocumentsByVehicule(1L)).thenReturn(List.of(docResponse));

        List<DocumentResponse> result = vehiculeService.getVehiculeDocuments(1L);

        assertFalse(result.isEmpty());
        verify(vehiculeDocumentService, times(1)).getDocumentsByVehicule(1L);
    }

    @Test
    void getVehiculeDocuments_ShouldThrowException_WhenVehiculeNotFound() {
        when(vehiculeRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> vehiculeService.getVehiculeDocuments(1L));
        verify(vehiculeDocumentService, never()).getDocumentsByVehicule(anyLong());
    }
}
