package com.ndaje.reservation.service.impl;

import com.ndaje.reservation.client.TripClient;
import com.ndaje.reservation.client.UserClient;
import com.ndaje.reservation.dto.ApiResponse;
import com.ndaje.reservation.dto.ReservationResponse;
import com.ndaje.reservation.dto.TripResponse;
import com.ndaje.reservation.dto.UserDto;
import com.ndaje.reservation.entity.Reservation;
import com.ndaje.reservation.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private TripClient tripClient;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @Test
    void getReservationsByDriverId_ShouldReturnEnrichedReservations() {
        // Arrange
        String driverId = "driver-123";
        TripResponse trip = new TripResponse();
        trip.setId(1L);
        trip.setDriverId(driverId);
        trip.setDepart("Dakar");
        trip.setArrivee("Saint-Louis");

        ApiResponse<List<TripResponse>> tripApiResponse = ApiResponse.<List<TripResponse>>builder()
                .success(true)
                .data(Collections.singletonList(trip))
                .build();

        when(tripClient.getTripsByDriverId(driverId)).thenReturn(ResponseEntity.ok(tripApiResponse));

        Reservation reservation = Reservation.builder()
                .id(101L)
                .tripId(1L)
                .passengerId("passenger-456")
                .places(2)
                .reservationDate(LocalDateTime.now())
                .build();

        when(reservationRepository.findByTripIdIn(anyList())).thenReturn(Collections.singletonList(reservation));

        UserDto passenger = new UserDto();
        passenger.setId("passenger-456");
        passenger.setPrenom("Alice");
        passenger.setNom("Bob");
        passenger.setTelephone("771234567");

        when(userClient.getUserById("passenger-456")).thenReturn(passenger);

        // Act
        List<ReservationResponse> results = reservationService.getReservationsByDriverId(driverId);

        // Assert
        assertNotNull(results);
        assertEquals(1, results.size());
        ReservationResponse res = results.get(0);
        assertEquals("Alice", res.getPassengerFirstName());
        assertEquals("Dakar", res.getDepart());
        assertEquals("771234567", res.getPassengerPhone());
    }
}
