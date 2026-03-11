package com.ndaje.trip.service.impl;

import com.ndaje.trip.client.CarClient;
import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.request.UpdateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.exception.TripNotFoundException;
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
class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private CarClient carClient;

    @InjectMocks
    private TripServiceImpl tripService;

    private Trajet trajet;
    private CreateTripRequest createRequest;

    @BeforeEach
    void setUp() {
        trajet = Trajet.builder()
                .id(1L)
                .driverId("driver-123")
                .vehicleId("456")
                .depart("Dakar")
                .arrivee("Thies")
                .dateDepart(LocalDateTime.now().plusDays(2))
                .placesDisponibles(4)
                .prix(3000.0)
                .statutTrajet(StatutTrajet.CREATED)
                .build();

        createRequest = new CreateTripRequest();
        createRequest.setDriverId("driver-123");
        createRequest.setVehicleId("456");
        createRequest.setDepart("Dakar");
        createRequest.setArrivee("Thies");
        createRequest.setDateDepart(LocalDateTime.now().plusDays(2));
        createRequest.setPlacesDisponibles(4);
        createRequest.setPrix(3000.0);
    }

    @Test
    void createTrip_shouldCreateSuccessfully() {
        when(userClient.getUserById(anyString())).thenReturn(mock(com.ndaje.trip.dto.response.UserDto.class));
        when(carClient.getVehiculeById(anyLong())).thenReturn(mock(com.ndaje.trip.dto.response.VehiculeDto.class));
        when(tripRepository.save(any(Trajet.class))).thenReturn(trajet);

        TripResponse response = tripService.createTrip(createRequest);

        assertNotNull(response);
        assertEquals("driver-123", response.getDriverId());
        verify(tripRepository, times(1)).save(any(Trajet.class));
    }

    @Test
    void createTrip_shouldThrowException_whenUserNotFound() {
        when(userClient.getUserById(anyString())).thenThrow(new RuntimeException());

        assertThrows(BusinessException.class, () -> tripService.createTrip(createRequest));
        verify(tripRepository, never()).save(any(Trajet.class));
    }

    @Test
    void getTripById_shouldReturnTrip() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trajet));

        TripResponse response = tripService.getTripById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void getTripById_shouldThrowException_whenNotFound() {
        when(tripRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> tripService.getTripById(2L));
    }

    @Test
    void getAllTrips_shouldReturnList() {
        when(tripRepository.findAll()).thenReturn(List.of(trajet));

        List<TripResponse> list = tripService.getAllTrips();

        assertFalse(list.isEmpty());
        assertEquals(1, list.size());
    }

    @Test
    void updateTripStatus_shouldUpdate() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(tripRepository.save(any(Trajet.class))).thenReturn(trajet);

        TripResponse response = tripService.updateTripStatus(1L, StatutTrajet.COMPLETED);

        assertNotNull(response);
        assertEquals(StatutTrajet.COMPLETED, trajet.getStatutTrajet());
    }

    @Test
    void decrementSeats_shouldDecrementSuccess() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(tripRepository.save(any(Trajet.class))).thenReturn(trajet);

        TripResponse response = tripService.decrementSeats(1L, 2);

        assertEquals(2, trajet.getPlacesDisponibles());
        verify(tripRepository, times(1)).save(any(Trajet.class));
    }

    @Test
    void decrementSeats_shouldThrowException_whenNotEnoughSeats() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trajet));

        assertThrows(BusinessException.class, () -> tripService.decrementSeats(1L, 5));
    }

    @Test
    void updateTrip_shouldUpdateFields() {
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trajet));
        when(tripRepository.save(any(Trajet.class))).thenReturn(trajet);

        UpdateTripRequest updateRequest = new UpdateTripRequest();
        updateRequest.setPrix(5000.0);

        TripResponse response = tripService.updateTrip(1L, updateRequest);

        assertNotNull(response);
        assertEquals(5000.0, trajet.getPrix());
    }
}
