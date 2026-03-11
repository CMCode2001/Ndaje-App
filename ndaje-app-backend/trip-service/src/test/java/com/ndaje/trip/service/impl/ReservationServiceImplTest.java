package com.ndaje.trip.service.impl;

import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.ReservationResponse;
import com.ndaje.trip.entity.Caravane;
import com.ndaje.trip.entity.Reservation;
import com.ndaje.trip.entity.StatutReservation;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.repository.CaravaneRepository;
import com.ndaje.trip.repository.ReservationRepository;
import com.ndaje.trip.repository.TripRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private TripRepository tripRepository;
    @Mock
    private CaravaneRepository caravaneRepository;
    @Mock
    private UserClient userClient;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private CreateReservationRequest requestTrajet;
    private CreateReservationRequest requestCaravane;
    private Trajet trajet;
    private Caravane caravane;
    private Reservation reservationTrajet;
    private Reservation reservationCaravane;

    @BeforeEach
    void setUp() {
        requestTrajet = new CreateReservationRequest("passenger-1", 10L, "TRAJET", 2);
        requestCaravane = new CreateReservationRequest("passenger-2", 20L, "CARAVANE", 3);

        trajet = Trajet.builder()
                .id(10L)
                .placesDisponibles(5)
                .build();

        caravane = Caravane.builder()
                .id(20L)
                .placesDisponibles(10)
                .build();

        reservationTrajet = Reservation.builder()
                .id(1L)
                .passengerId("passenger-1")
                .voyageId(10L)
                .typeVoyage("TRAJET")
                .places(2)
                .reservationDate(LocalDateTime.now())
                .status(StatutReservation.CONFIRMED)
                .build();

        reservationCaravane = Reservation.builder()
                .id(2L)
                .passengerId("passenger-2")
                .voyageId(20L)
                .typeVoyage("CARAVANE")
                .places(3)
                .reservationDate(LocalDateTime.now())
                .status(StatutReservation.CONFIRMED)
                .build();
    }

    @Test
    void createReservation_Trajet_ShouldCreateSuccessfully() {
        when(userClient.getUserById(anyString())).thenReturn(mock(com.ndaje.trip.dto.response.UserDto.class));
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trajet));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationTrajet);

        ReservationResponse response = reservationService.createReservation(requestTrajet);

        assertNotNull(response);
        assertEquals("passenger-1", response.getPassengerId());
        assertEquals("TRAJET", response.getTypeVoyage());
        assertEquals(3, trajet.getPlacesDisponibles()); // 5 - 2
        verify(tripRepository, times(1)).save(trajet);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void createReservation_Caravane_ShouldCreateSuccessfully() {
        when(userClient.getUserById(anyString())).thenReturn(mock(com.ndaje.trip.dto.response.UserDto.class));
        when(caravaneRepository.findById(anyLong())).thenReturn(Optional.of(caravane));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservationCaravane);

        ReservationResponse response = reservationService.createReservation(requestCaravane);

        assertNotNull(response);
        assertEquals("passenger-2", response.getPassengerId());
        assertEquals("CARAVANE", response.getTypeVoyage());
        assertEquals(7, caravane.getPlacesDisponibles()); // 10 - 3
        verify(caravaneRepository, times(1)).save(caravane);
        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowException_WhenUserNotFound() {
        when(userClient.getUserById(anyString())).thenThrow(new RuntimeException("User not found"));

        assertThrows(BusinessException.class, () -> reservationService.createReservation(requestTrajet));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_ShouldThrowException_WhenNotEnoughPlacesInTrajet() {
        requestTrajet.setPlaces(10);
        when(userClient.getUserById(anyString())).thenReturn(mock(com.ndaje.trip.dto.response.UserDto.class));
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trajet));

        assertThrows(BusinessException.class, () -> reservationService.createReservation(requestTrajet));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void getReservationsByPassengerId_ShouldReturnList() {
        when(reservationRepository.findByPassengerId("passenger-1")).thenReturn(List.of(reservationTrajet));

        List<ReservationResponse> responses = reservationService.getReservationsByPassengerId("passenger-1");

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals("passenger-1", responses.get(0).getPassengerId());
    }

    @Test
    void cancelReservation_Trajet_ShouldCancelAndRestitutePlaces() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationTrajet));
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trajet));

        reservationService.cancelReservation(1L, "passenger-1");

        assertEquals(StatutReservation.CANCELLED, reservationTrajet.getStatus());
        assertEquals(7, trajet.getPlacesDisponibles()); // 5 + 2 restituted places
        verify(reservationRepository, times(1)).save(reservationTrajet);
        verify(tripRepository, times(1)).save(trajet);
    }

    @Test
    void cancelReservation_Caravane_ShouldCancelAndRestitutePlaces() {
        when(reservationRepository.findById(2L)).thenReturn(Optional.of(reservationCaravane));
        when(caravaneRepository.findById(20L)).thenReturn(Optional.of(caravane));

        reservationService.cancelReservation(2L, "passenger-2");

        assertEquals(StatutReservation.CANCELLED, reservationCaravane.getStatus());
        assertEquals(13, caravane.getPlacesDisponibles()); // 10 + 3 restituted places
        verify(reservationRepository, times(1)).save(reservationCaravane);
        verify(caravaneRepository, times(1)).save(caravane);
    }

    @Test
    void cancelReservation_ShouldThrowException_WhenNotPassenger() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationTrajet));

        assertThrows(BusinessException.class, () -> reservationService.cancelReservation(1L, "wrong-passenger"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void cancelReservation_ShouldThrowException_WhenAlreadyCancelled() {
        reservationTrajet.setStatus(StatutReservation.CANCELLED);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservationTrajet));

        assertThrows(BusinessException.class, () -> reservationService.cancelReservation(1L, "passenger-1"));
        verify(reservationRepository, never()).save(any(Reservation.class));
    }
}
