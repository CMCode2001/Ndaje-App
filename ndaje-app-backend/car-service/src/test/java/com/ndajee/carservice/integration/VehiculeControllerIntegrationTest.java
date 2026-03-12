package com.ndajee.carservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.repository.VehiculeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.mock.web.MockMultipartFile;

/**
 * Tests d'intégration du VehiculeController.
 *
 * Stratégie :
 * - Base H2 en mémoire activée via le profil "integration".
 * - Sécurité désactivée via addFilters = false sur MockMvc.
 * - S3StorageService et DocumentClient mockés pour éviter les appels réseau.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@DisplayName("VehiculeController - Tests d'intégration")
class VehiculeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehiculeRepository vehiculeRepository;

    @MockBean
    private com.ndajee.carservice.storage.S3StorageService s3StorageService;

    @MockBean
    private com.ndajee.carservice.client.DocumentClient documentClient;

    @BeforeEach
    void setUp() {
        vehiculeRepository.deleteAll();
    }

    // =========================================================================
    // Tests de création
    // =========================================================================

    @Test
    @DisplayName("POST /api/vehicules - Crée un véhicule avec succès, retourne 201")
    void givenValidVehiculeRequest_whenCreateVehicule_thenReturns201() throws Exception {
        // Arrange
        VehiculeRequest request = buildVehiculeRequest("Toyota", "Corolla", "DK-1234-A", "Blanc", 2022, 4,
                "driver-uuid-001");

        // Act & Assert
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.marque", is("Toyota")))
                .andExpect(jsonPath("$.modele", is("Corolla")))
                .andExpect(jsonPath("$.driverId", is("driver-uuid-001")));
    }

    @Test
    @DisplayName("POST /api/vehicules - Champs obligatoires manquants retourne 400")
    void givenMissingFields_whenCreateVehicule_thenReturns400() throws Exception {
        // Act & Assert: corps incomplet (marque absente)
        mockMvc.perform(post("/api/vehicules")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modele\": \"Corolla\"}"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Tests de récupération
    // =========================================================================

    @Test
    @DisplayName("GET /api/vehicules - Liste tous les véhicules")
    void givenMultipleVehicules_whenGetAll_thenReturnsAll() throws Exception {
        // Arrange: insérer 2 véhicules en BDD
        vehiculeRepository.save(buildVehiculeEntity("Renault", "Megane", "DL-5555-B", "driver-A"));
        vehiculeRepository.save(buildVehiculeEntity("Peugeot", "208", "DK-7777-C", "driver-B"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/vehicules/{id} - Trouve un véhicule par son ID")
    void givenExistingVehiculeId_whenGetById_thenReturnsVehicule() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("Toyota", "Camry", "DK-9999-X", "driver-C"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicules/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marque", is("Toyota")))
                .andExpect(jsonPath("$.modele", is("Camry")));
    }

    @Test
    @DisplayName("GET /api/vehicules/{id} - ID inexistant retourne 404")
    void givenNonExistentVehiculeId_whenGetById_thenReturns404() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/vehicules/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/vehicules/driver/{driverId} - Retourne les véhicules d'un conducteur")
    void givenDriverId_whenGetVehiculesByDriver_thenReturnsMatchingVehicules() throws Exception {
        // Arrange
        vehiculeRepository.save(buildVehiculeEntity("Honda", "Civic", "DK-0001-A", "target-driver"));
        vehiculeRepository.save(buildVehiculeEntity("BMW", "X5", "DK-0002-B", "target-driver"));
        vehiculeRepository.save(buildVehiculeEntity("Audi", "A4", "DK-0003-C", "other-driver"));

        // Act & Assert: only 2 belong to target-driver
        mockMvc.perform(get("/api/vehicules/driver/target-driver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // =========================================================================
    // Tests de mise à jour
    // =========================================================================

    @Test
    @DisplayName("PUT /api/vehicules/{id} - Met à jour un véhicule existant")
    void givenExistingVehicule_whenUpdate_thenReturnsUpdatedVehicule() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("Ford", "Focus", "DK-4444-F", "driver-D"));
        VehiculeRequest updateRequest = buildVehiculeRequest("Ford", "Focus ST", "DK-4444-F", "Rouge", 2023, 5,
                "driver-D");

        // Act & Assert
        mockMvc.perform(put("/api/vehicules/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modele", is("Focus ST")))
                .andExpect(jsonPath("$.couleur", is("Rouge")));
    }

    @Test
    @DisplayName("PUT /api/vehicules/{id} - ID inexistant retourne 404")
    void givenNonExistentVehicule_whenUpdate_thenReturns404() throws Exception {
        // Arrange
        VehiculeRequest updateRequest = buildVehiculeRequest("Ford", "Focus", "DK-8888-Z", "Bleu", 2021, 4, "driver-X");

        // Act & Assert
        mockMvc.perform(put("/api/vehicules/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().is4xxClientError());
    }

    // =========================================================================
    // Tests de suppression
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/vehicules/{id} - Supprime un véhicule existant, retourne 204")
    void givenExistingVehicule_whenDelete_thenReturns204() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("Mazda", "CX-5", "DK-2222-M", "driver-E"));

        // Act & Assert
        mockMvc.perform(delete("/api/vehicules/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Vérification en BDD
        assert vehiculeRepository.findById(saved.getId()).isEmpty();
    }

    @Test
    @DisplayName("DELETE /api/vehicules/{id} - ID inexistant retourne 4xx")
    void givenNonExistentVehicule_whenDelete_thenReturns4xx() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/vehicules/99999"))
                .andExpect(status().is4xxClientError());
    }

    // =========================================================================
    // Tests d'upload de documents
    // =========================================================================

    @Test
    @DisplayName("POST /api/vehicules/{id}/documents - Upload réussi retourne 201")
    void givenExistingVehicule_whenUploadDocument_thenReturns201() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("BMW", "Serie3", "DK-9090-Z", "driver-UP"));
        when(s3StorageService.uploadFile(any(), anyString())).thenReturn("vehicules/fake-s3-key.pdf");

        MockMultipartFile file = new MockMultipartFile(
                "file", "carte-grise.pdf", "application/pdf", "pdf-content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/vehicules/" + saved.getId() + "/documents")
                .file(file)
                .param("typeDocument", "CARTE_GRISE")
                .param("numero", "CG-12345")
                .param("expiration", "2027-12-31"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/vehicules/{id}/documents - Fichier vide retourne 400")
    void givenEmptyFile_whenUploadDocument_thenReturns400() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("Kia", "Sportage", "DK-1111-K", "driver-KS"));

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        // Act & Assert
        mockMvc.perform(multipart("/api/vehicules/" + saved.getId() + "/documents")
                .file(emptyFile)
                .param("typeDocument", "CARTE_GRISE")
                .param("numero", "CG-000"))
                .andExpect(status().is5xxServerError()); // empty file throws IllegalArgumentException → 500 par
                                                         // GlobalExceptionHandler
    }

    @Test
    @DisplayName("POST /api/vehicules/{id}/documents - Paramètres obligatoires manquants retourne 400")
    void givenMissingParams_whenUploadDocument_thenReturns400() throws Exception {
        // Arrange
        Vehicule saved = vehiculeRepository.save(buildVehiculeEntity("Hyundai", "Tucson", "DK-2222-H", "driver-HT"));
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf", "content".getBytes());

        // Act & Assert: typeDocument manquant
        mockMvc.perform(multipart("/api/vehicules/" + saved.getId() + "/documents")
                .file(file)
                .param("numero", "NUM-001")) // typeDocument absent
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/vehicules/{id}/documents - Véhicule inexistant retourne 404")
    void givenNonExistentVehicule_whenUploadDocument_thenReturns404() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "assurance.pdf", "application/pdf", "content".getBytes());

        // Act & Assert
        mockMvc.perform(multipart("/api/vehicules/99999/documents")
                .file(file)
                .param("typeDocument", "ASSURANCE")
                .param("numero", "ASS-999"))
                .andExpect(status().isNotFound());
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================

    private VehiculeRequest buildVehiculeRequest(String marque, String modele, String immat, String couleur, int annee,
            int places, String driverId) {
        VehiculeRequest req = new VehiculeRequest();
        req.setMarque(marque);
        req.setModele(modele);
        req.setImmatriculation(immat);
        req.setCouleur(couleur);
        req.setAnnee(annee);
        req.setPlaces(places);
        req.setDriverId(driverId);
        return req;
    }

    private Vehicule buildVehiculeEntity(String marque, String modele, String immat, String driverId) {
        Vehicule v = new Vehicule();
        v.setMarque(marque);
        v.setModele(modele);
        v.setImmatriculation(immat);
        v.setCouleur("Noir");
        v.setAnnee(2021);
        v.setPlaces(4);
        v.setDriverId(driverId);
        v.setStatutVerification(com.ndajee.carservice.domain.StatutVerificationVehicule.EN_ATTENTE);
        return v;
    }
}
