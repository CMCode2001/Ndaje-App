package com.ndaje.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndaje.trip.dto.request.CreateNotationRequest;
import com.ndaje.trip.entity.Notation;
import com.ndaje.trip.service.NotationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotationService notationService;

    @InjectMocks
    private NotationController notationController;

    private ObjectMapper objectMapper;
    private CreateNotationRequest createRequest;
    private Notation notation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(notationController).build();
        objectMapper = new ObjectMapper();

        createRequest = new CreateNotationRequest(1L, 10L, "passenger-1", 5, "Très bien");

        notation = Notation.builder()
                .id(100L)
                .reservationId(1L)
                .trajetId(10L)
                .passagerId("passenger-1")
                .etoiles(5)
                .commentaire("Très bien")
                .build();
    }

    @Test
    void createNotation_ShouldReturnCreated() throws Exception {
        when(notationService.createNotation(any(CreateNotationRequest.class))).thenReturn(notation);

        mockMvc.perform(post("/api/notations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.etoiles").value(5));

        verify(notationService, times(1)).createNotation(any(CreateNotationRequest.class));
    }

    @Test
    void getNotationsByTrajet_ShouldReturnList() throws Exception {
        when(notationService.getNotationsByTrajet(10L)).thenReturn(List.of(notation));

        mockMvc.perform(get("/api/notations/trajet/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100));
    }

    @Test
    void getNotationsByReservation_ShouldReturnList() throws Exception {
        when(notationService.getNotationsByReservation(1L)).thenReturn(List.of(notation));

        mockMvc.perform(get("/api/notations/reservation/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(100));
    }

    @Test
    void getMoyenneTrajet_ShouldReturnAverage() throws Exception {
        when(notationService.getMoyenneParTrajet(10L)).thenReturn(4.5);

        mockMvc.perform(get("/api/notations/trajet/10/moyenne"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(4.5));
    }
}
