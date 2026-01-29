package com.ndaje.trip.service.impl;

import com.ndaje.trip.client.CarClient;
import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.dto.response.UserDto;
import com.ndaje.trip.dto.response.VehiculeDto;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceImplTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private CarClient carClient;

    @InjectMocks
    private TripServiceImpl tripService;

    private CreateTripRequest validRequest;
    private VehiculeDto mockVehicle;
    private UserDto mockUser;

    @BeforeEach
    void setUp() {
        validRequest = new CreateTripRequest(
                "driver-1",
                "1",
                "Dakar",
                "Thies",
                LocalDateTime.now().plusDays(1),
                4,
                5000.0
        );

        mockVehicle = new VehiculeDto();
        mockVehicle.setId(1L);
        mockVehicle.setMarque("Toyota");
        mockVehicle.setModele("Corolla");
        mockVehicle.setImmatriculation("DK-123-AB");
        mockVehicle.setPlaces(4);

        mockUser = new UserDto();
        mockUser.setId("driver-1");
        mockUser.setPrenom("John");
        mockUser.setNom("Doe");
    }

    @Test
    void createTrip_WhenPlacesOk_ShouldSucceed() {
        // Arrange
        when(userClient.getUserById("driver-1")).thenReturn(mockUser);
        when(carClient.getVehiculeById(1L)).thenReturn(mockVehicle);
        when(tripRepository.save(any(Trajet.class))).thenAnswer(invocation -> {
            Trajet t = invocation.getArgument(0);
            t.setId(100L);
            return t;
        });

        // Act
        TripResponse response = tripService.createTrip(validRequest);

        // Verify
        assertNotNull(response);
        assertEquals("Toyota", response.getVehicleMarque());
        assertEquals("John", response.getDriverFirstName());
        verify(tripRepository).save(any(Trajet.class));
    }

    @Test
    void createTrip_WhenPlacesExceedCapacity_ShouldThrowException() {
        // Arrange
        validRequest.setPlacesDisponibles(5); // Requesting 5 seats
        when(userClient.getUserById("driver-1")).thenReturn(mockUser);
        when(carClient.getVehiculeById(1L)).thenReturn(mockVehicle); // Vehicle has only 4 seats

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            tripService.createTrip(validRequest);
        });

        assertTrue(exception.getMessage().contains("cannot exceed vehicle capacity"));
        verify(tripRepository, never()).save(any(Trajet.class));
    }
}
