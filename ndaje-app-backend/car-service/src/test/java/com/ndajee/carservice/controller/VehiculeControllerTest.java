package com.ndajee.carservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import com.ndajee.carservice.service.VehiculeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehiculeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VehiculeService vehiculeService;

    @InjectMocks
    private VehiculeController vehiculeController;

    private ObjectMapper objectMapper;
    private VehiculeRequest vehiculeRequest;
    private VehiculeResponse vehiculeResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(vehiculeController).build();
        objectMapper = new ObjectMapper();

        vehiculeRequest = new VehiculeRequest();
        vehiculeRequest.setDriverId("driver-1");
        vehiculeRequest.setMarque("Toyota");
        vehiculeRequest.setModele("Corolla");
        vehiculeRequest.setAnnee(2020);

        vehiculeResponse = new VehiculeResponse();
        vehiculeResponse.setId(1L);
        vehiculeResponse.setDriverId("driver-1");
        vehiculeResponse.setMarque("Toyota");
        vehiculeResponse.setModele("Corolla");
    }

    @Test
    void createVehicule_ShouldReturnCreated() throws Exception {
        when(vehiculeService.createVehicule(any(VehiculeRequest.class))).thenReturn(vehiculeResponse);

        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.marque").value("Toyota"));
    }

    @Test
    void updateVehicule_ShouldReturnOk() throws Exception {
        when(vehiculeService.updateVehicule(eq(1L), any(VehiculeRequest.class))).thenReturn(vehiculeResponse);

        mockMvc.perform(put("/api/vehicules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehiculeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllVehicules_ShouldReturnList() throws Exception {
        when(vehiculeService.getAllVehicules()).thenReturn(List.of(vehiculeResponse));

        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getVehiculeById_ShouldReturnVehicule() throws Exception {
        when(vehiculeService.getVehiculeById(1L)).thenReturn(Optional.of(vehiculeResponse));

        mockMvc.perform(get("/api/vehicules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getVehiculesByDriverId_ShouldReturnList() throws Exception {
        when(vehiculeService.getVehiculesByDriverId("driver-1")).thenReturn(List.of(vehiculeResponse));

        mockMvc.perform(get("/api/vehicules/driver/driver-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].driverId").value("driver-1"));
    }

    @Test
    void deleteVehicule_ShouldReturnNoContent() throws Exception {
        doNothing().when(vehiculeService).deleteVehicule(1L);

        mockMvc.perform(delete("/api/vehicules/1"))
                .andExpect(status().isNoContent());

        verify(vehiculeService, times(1)).deleteVehicule(1L);
    }

    @Test
    void uploadDocument_ShouldReturnCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cert.pdf", "application/pdf", "data".getBytes());
        DocumentResponse docResponse = new DocumentResponse();
        docResponse.setId(10L);

        when(vehiculeService.uploadDocument(eq(1L), any(MultipartFile.class), anyString(), anyString(), anyString()))
                .thenReturn(docResponse);

        mockMvc.perform(multipart("/api/vehicules/1/documents")
                .file(file)
                .param("typeDocument", "ASSURANCE")
                .param("numero", "12345")
                .param("expiration", "2025-12-31"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void getVehiculeDocuments_ShouldReturnList() throws Exception {
        DocumentResponse docResponse = new DocumentResponse();
        docResponse.setId(10L);

        when(vehiculeService.getVehiculeDocuments(1L)).thenReturn(List.of(docResponse));

        mockMvc.perform(get("/api/vehicules/1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }
}
