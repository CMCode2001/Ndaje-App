package com.ndaje.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.request.UpdateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.service.TripService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TripControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TripService tripService;

    @InjectMocks
    private TripController tripController;

    private ObjectMapper objectMapper;
    private CreateTripRequest createRequest;
    private TripResponse tripResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(tripController).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        createRequest = new CreateTripRequest();
        createRequest.setDriverId("driver-123");
        createRequest.setVehicleId("veh-456");
        createRequest.setDepart("Dakar");
        createRequest.setArrivee("Thies");
        createRequest.setDateDepart(LocalDateTime.now().plusDays(2));
        createRequest.setPlacesDisponibles(4);
        createRequest.setPrix(3000.0);

        tripResponse = TripResponse.builder()
                .id(1L)
                .driverId("driver-123")
                .vehicleId("veh-456")
                .depart("Dakar")
                .arrivee("Thies")
                .dateDepart(LocalDateTime.now().plusDays(2))
                .placesDisponibles(4)
                .prix(3000.0)
                .statutTrajet(StatutTrajet.CREATED)
                .build();
    }

    @Test
    void createTrip_ShouldReturnCreated() throws Exception {
        when(tripService.createTrip(any(CreateTripRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.depart").value("Dakar"));

        verify(tripService, times(1)).createTrip(any(CreateTripRequest.class));
    }

    @Test
    void getTripById_ShouldReturnTrip() throws Exception {
        when(tripService.getTripById(1L)).thenReturn(tripResponse);

        mockMvc.perform(get("/api/trips/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getAllTrips_ShouldReturnList() throws Exception {
        when(tripService.getAllTrips()).thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void updateTripStatus_ShouldReturnUpdated() throws Exception {
        tripResponse.setStatutTrajet(StatutTrajet.COMPLETED);
        when(tripService.updateTripStatus(1L, StatutTrajet.COMPLETED)).thenReturn(tripResponse);

        mockMvc.perform(patch("/api/trips/1/status")
                .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.statutTrajet").value("COMPLETED"));
    }

    @Test
    void getTripsByDriver_ShouldReturnList() throws Exception {
        when(tripService.getTripsByDriverId("driver-123")).thenReturn(List.of(tripResponse));

        mockMvc.perform(get("/api/trips/driver/driver-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].driverId").value("driver-123"));
    }

    @Test
    void updateTrip_ShouldReturnUpdated() throws Exception {
        UpdateTripRequest updateRequest = new UpdateTripRequest();
        updateRequest.setPrix(5000.0);

        tripResponse.setPrix(5000.0);

        when(tripService.updateTrip(eq(1L), any(UpdateTripRequest.class))).thenReturn(tripResponse);

        mockMvc.perform(put("/api/trips/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.prix").value(5000.0));
    }

    @Test
    void decrementSeats_ShouldReturnUpdated() throws Exception {
        tripResponse.setPlacesDisponibles(2);
        when(tripService.decrementSeats(1L, 2)).thenReturn(tripResponse);

        mockMvc.perform(post("/api/trips/1/decrement-seats")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.placesDisponibles").value(2));
    }
}
