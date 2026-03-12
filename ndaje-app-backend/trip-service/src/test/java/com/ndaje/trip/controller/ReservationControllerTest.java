package com.ndaje.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.ReservationResponse;
import com.ndaje.trip.service.ReservationService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController reservationController;

    private ObjectMapper objectMapper;
    private CreateReservationRequest createRequest;
    private ReservationResponse reservationResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController).build();
        objectMapper = new ObjectMapper();

        createRequest = new CreateReservationRequest("passenger-1", 10L, "TRAJET", 2);

        reservationResponse = ReservationResponse.builder()
                .id(1L)
                .passengerId("passenger-1")
                .voyageId(10L)
                .typeVoyage("TRAJET")
                .places(2)
                .reservationDate(LocalDateTime.now())
                .status("CONFIRMED")
                .build();
    }

    @Test
    void createReservation_ShouldReturnCreated() throws Exception {
        when(reservationService.createReservation(any(CreateReservationRequest.class))).thenReturn(reservationResponse);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.passengerId").value("passenger-1"));

        verify(reservationService, times(1)).createReservation(any(CreateReservationRequest.class));
    }

    @Test
    void getReservationsByPassengerId_ShouldReturnList() throws Exception {
        when(reservationService.getReservationsByPassengerId(anyString())).thenReturn(List.of(reservationResponse));

        mockMvc.perform(get("/api/reservations/passenger/passenger-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void cancelReservation_ShouldReturnOk() throws Exception {
        doNothing().when(reservationService).cancelReservation(1L);

        mockMvc.perform(delete("/api/reservations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reservationService, times(1)).cancelReservation(1L);
    }
}
