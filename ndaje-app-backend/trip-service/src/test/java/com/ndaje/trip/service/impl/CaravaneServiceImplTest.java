package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateCaravaneRequest;
import com.ndaje.trip.dto.response.CaravaneResponse;
import com.ndaje.trip.entity.Caravane;
import com.ndaje.trip.entity.StatutCaravane;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.repository.CaravaneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaravaneServiceImplTest {

    @Mock
    private CaravaneRepository caravaneRepository;

    @InjectMocks
    private CaravaneServiceImpl caravaneService;

    private CreateCaravaneRequest createRequest;
    private Caravane caravane;

    @BeforeEach
    void setUp() {
        createRequest = new CreateCaravaneRequest();
        createRequest.setCaravannierId("caravannier-123");
        createRequest.setNom("Caravane Dakar-Tambacounda");
        createRequest.setDescription("Voyage annuel");
        createRequest.setDepart("Dakar");
        createRequest.setArrivee("Tambacounda");
        createRequest.setEtapes("Mbour, Kaolack");
        createRequest.setDateDepart(LocalDateTime.now().plusDays(5));
        createRequest.setDateArriveeEstimee(LocalDateTime.now().plusDays(6));
        createRequest.setMaxParticipants(50);
        createRequest.setPrixParPersonne(15000.0);
        createRequest.setVehiculeIds("veh1,veh2");

        caravane = Caravane.builder()
                .id(1L)
                .caravannierId("caravannier-123")
                .nom("Caravane Dakar-Tambacounda")
                .description("Voyage annuel")
                .depart("Dakar")
                .arrivee("Tambacounda")
                .etapes("Mbour, Kaolack")
                .dateDepart(LocalDateTime.now().plusDays(5))
                .dateArriveeEstimee(LocalDateTime.now().plusDays(6))
                .maxParticipants(50)
                .placesDisponibles(50)
                .prixParPersonne(15000.0)
                .vehiculeIds("veh1,veh2")
                .statut(StatutCaravane.OUVERTE)
                .dateCreation(LocalDateTime.now())
                .build();
    }

    @Test
    void createCaravane_shouldCreateCaravaneSuccessfully() {
        when(caravaneRepository.save(any(Caravane.class))).thenReturn(caravane);

        CaravaneResponse response = caravaneService.createCaravane(createRequest);

        assertNotNull(response);
        assertEquals(createRequest.getNom(), response.getNom());
        assertEquals("caravannier-123", response.getCaravannierId());
        verify(caravaneRepository, times(1)).save(any(Caravane.class));
    }

    @Test
    void createCaravane_shouldThrowException_whenDateArriveeEstimeeIsBeforeDepart() {
        createRequest.setDateArriveeEstimee(LocalDateTime.now().minusDays(1));

        assertThrows(BusinessException.class, () -> caravaneService.createCaravane(createRequest));
        verify(caravaneRepository, never()).save(any(Caravane.class));
    }

    @Test
    void getCaravaneById_shouldReturnCaravane() {
        when(caravaneRepository.findById(1L)).thenReturn(Optional.of(caravane));

        CaravaneResponse response = caravaneService.getCaravaneById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getCaravaneById_shouldThrowException_whenNotFound() {
        when(caravaneRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> caravaneService.getCaravaneById(2L));
    }

    @Test
    void getCaravanesOuvertes_shouldReturnOuvertes() {
        when(caravaneRepository.findByStatutAndPlacesDisponiblesGreaterThanAndDateDepartAfter(
                eq(StatutCaravane.OUVERTE), eq(0), any(LocalDateTime.class)))
                .thenReturn(List.of(caravane));

        List<CaravaneResponse> list = caravaneService.getCaravanesOuvertes();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void getCaravanesByCaravannier_shouldReturnList() {
        when(caravaneRepository.findByCaravannierId("caravannier-123")).thenReturn(List.of(caravane));

        List<CaravaneResponse> list = caravaneService.getCaravanesByCaravannier("caravannier-123");

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void searchCaravanes_shouldReturnList() {
        when(caravaneRepository.findByDepartIgnoreCaseOrArriveeIgnoreCase("Dakar", "Dakar"))
                .thenReturn(List.of(caravane));

        List<CaravaneResponse> list = caravaneService.searchCaravanes("Dakar");

        assertFalse(list.isEmpty());
    }

    @Test
    void cancelCaravane_shouldCancel() {
        when(caravaneRepository.findById(1L)).thenReturn(Optional.of(caravane));
        when(caravaneRepository.save(any(Caravane.class))).thenReturn(caravane);

        caravaneService.cancelCaravane(1L, "caravannier-123");

        assertEquals(StatutCaravane.ANNULEE, caravane.getStatut());
        verify(caravaneRepository, times(1)).save(caravane);
    }

    @Test
    void cancelCaravane_shouldThrowException_whenNotOwner() {
        when(caravaneRepository.findById(1L)).thenReturn(Optional.of(caravane));

        assertThrows(BusinessException.class, () -> caravaneService.cancelCaravane(1L, "other-user"));
        verify(caravaneRepository, never()).save(any(Caravane.class));
    }

    @Test
    void cancelCaravane_shouldThrowException_whenAlreadyEnCours() {
        caravane.setStatut(StatutCaravane.EN_COURS);
        when(caravaneRepository.findById(1L)).thenReturn(Optional.of(caravane));

        assertThrows(BusinessException.class, () -> caravaneService.cancelCaravane(1L, "caravannier-123"));
        verify(caravaneRepository, never()).save(any(Caravane.class));
    }
}
