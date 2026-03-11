package com.ndaje.trip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ndaje.trip.dto.request.CreateCaravaneRequest;
import com.ndaje.trip.dto.response.CaravaneResponse;
import com.ndaje.trip.service.CaravaneService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CaravaneControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CaravaneService caravaneService;

    @InjectMocks
    private CaravaneController caravaneController;

    private ObjectMapper objectMapper;
    private CreateCaravaneRequest createRequest;
    private CaravaneResponse caravaneResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(caravaneController).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        createRequest = new CreateCaravaneRequest();
        createRequest.setCaravannierId("cara-123");
        createRequest.setNom("Voyage Dakar-Tambacounda");
        createRequest.setDepart("Dakar");
        createRequest.setArrivee("Tambacounda");
        createRequest.setDateDepart(LocalDateTime.now().plusDays(5));
        createRequest.setMaxParticipants(50);

        caravaneResponse = CaravaneResponse.builder()
                .id(1L)
                .caravannierId("cara-123")
                .nom("Voyage Dakar-Tambacounda")
                .depart("Dakar")
                .arrivee("Tambacounda")
                .maxParticipants(50)
                .build();
    }

    @Test
    void createCaravane_ShouldReturnCreated() throws Exception {
        when(caravaneService.createCaravane(any(CreateCaravaneRequest.class))).thenReturn(caravaneResponse);

        mockMvc.perform(post("/api/caravanes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.nom").value("Voyage Dakar-Tambacounda"));

        verify(caravaneService, times(1)).createCaravane(any(CreateCaravaneRequest.class));
    }

    @Test
    void getCaravaneById_ShouldReturnCaravane() throws Exception {
        when(caravaneService.getCaravaneById(1L)).thenReturn(caravaneResponse);

        mockMvc.perform(get("/api/caravanes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getCaravanesOuvertes_ShouldReturnList() throws Exception {
        when(caravaneService.getCaravanesOuvertes()).thenReturn(List.of(caravaneResponse));

        mockMvc.perform(get("/api/caravanes/ouvertes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1));
    }

    @Test
    void getCaravanesByCaravannier_ShouldReturnList() throws Exception {
        when(caravaneService.getCaravanesByCaravannier("cara-123")).thenReturn(List.of(caravaneResponse));

        mockMvc.perform(get("/api/caravanes/caravannier/cara-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].caravannierId").value("cara-123"));
    }

    @Test
    void searchCaravanes_ShouldReturnList() throws Exception {
        when(caravaneService.searchCaravanes("Dakar")).thenReturn(List.of(caravaneResponse));

        mockMvc.perform(get("/api/caravanes/search").param("ville", "Dakar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].depart").value("Dakar"));
    }

    @Test
    void cancelCaravane_ShouldReturnOk() throws Exception {
        doNothing().when(caravaneService).cancelCaravane(1L, "cara-123");

        mockMvc.perform(delete("/api/caravanes/1").param("caravannierId", "cara-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(caravaneService, times(1)).cancelCaravane(1L, "cara-123");
    }
}
