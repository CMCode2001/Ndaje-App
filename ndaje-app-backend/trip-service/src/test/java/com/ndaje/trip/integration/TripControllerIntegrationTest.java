package com.ndaje.trip.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndaje.trip.client.CarClient;
import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.UserDto;
import com.ndaje.trip.dto.response.VehiculeDto;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.repository.TripRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration du TripController.
 * Stratégie :
 * - @SpringBootTest démarre le contexte complet avec profil "integration".
 * - SecurityConfig remplacée par TestSecurityConfig (permissive).
 * - SecurityContext rempli manuellement pour les opérations sécurisées (BOLA).
 * - CarClient et UserClient Feign mockés avec @MockBean.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@DisplayName("TripController - Tests d'intégration")
class TripControllerIntegrationTest {

    static final String DRIVER_ID = "driver-uuid-001";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @MockBean
    private CarClient carClient;

    @MockBean
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();

        // Configure security context: the "current user" is DRIVER_ID
        mockSecurityContext(DRIVER_ID);

        // Stub UserClient
        UserDto fakeUser = new UserDto();
        fakeUser.setId(DRIVER_ID);
        fakeUser.setEmail("driver@ndajee.com");
        fakeUser.setRole("DRIVER");
        when(userClient.getUserById(anyString())).thenReturn(fakeUser);

        // Stub CarClient
        VehiculeDto fakeVehicle = new VehiculeDto();
        fakeVehicle.setId(1L);
        fakeVehicle.setDriverId(DRIVER_ID);
        fakeVehicle.setMarque("Toyota");
        fakeVehicle.setModele("Corolla");
        fakeVehicle.setImmatriculation("DK-7412-A");
        when(carClient.getVehiculeById(anyLong())).thenReturn(fakeVehicle);
        when(carClient.getVehiculesByDriverId(anyString())).thenReturn(List.of(fakeVehicle));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // Tests de création
    // =========================================================================

    @Test
    @DisplayName("POST /api/trips - Crée un trajet avec succès, retourne 201")
    void givenValidTripRequest_whenCreateTrip_thenReturns201WithTripData() throws Exception {
        CreateTripRequest request = buildCreateTripRequest("Dakar", "Thies", 5000.0, 4, DRIVER_ID, "1");

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.depart", is("Dakar")))
                .andExpect(jsonPath("$.data.arrivee", is("Thies")));
    }

    @Test
    @DisplayName("POST /api/trips - Corps invalide (champs manquants) retourne 400")
    void givenMissingFields_whenCreateTrip_thenReturns400() throws Exception {
        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"prix\": 1000}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trips - Prix négatif retourne 400")
    void givenNegativePrice_whenCreateTrip_thenReturns400() throws Exception {
        CreateTripRequest request = buildCreateTripRequest("Dakar", "Kaolack", -500.0, 3, DRIVER_ID, "1");

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trips - depart de 1 caractère retourne 400")
    void givenTooShortDepart_whenCreateTrip_thenReturns400() throws Exception {
        CreateTripRequest request = buildCreateTripRequest("D", "Kaolack", 2000.0, 2, DRIVER_ID, "1");

        mockMvc.perform(post("/api/trips")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Tests de récupération
    // =========================================================================

    @Test
    @DisplayName("GET /api/trips/{id} - Récupère un trajet existant")
    void givenExistingTripId_whenGetById_thenReturnsTripData() throws Exception {
        Trajet saved = tripRepository.save(buildTripEntity("Saint-Louis", "Ziguinchor", 8000.0, DRIVER_ID, "veh-X"));

        mockMvc.perform(get("/api/trips/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.depart", is("Saint-Louis")))
                .andExpect(jsonPath("$.data.arrivee", is("Ziguinchor")));
    }

    @Test
    @DisplayName("GET /api/trips/{id} - ID inexistant retourne 404")
    void givenNonExistentTripId_whenGetById_thenReturns404() throws Exception {
        mockMvc.perform(get("/api/trips/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/trips - Récupère tous les trajets")
    void givenMultipleTrips_whenGetAll_thenReturnsAllTrips() throws Exception {
        tripRepository.save(buildTripEntity("Dakar", "Thies", 5000.0, DRIVER_ID, "veh-A"));
        tripRepository.save(buildTripEntity("Kaolack", "Touba", 3500.0, DRIVER_ID, "veh-B"));
        tripRepository.save(buildTripEntity("Ziguinchor", "Dakar", 12000.0, DRIVER_ID, "veh-C"));

        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/trips/driver/{driverId} - Récupère uniquement les trajets du conducteur")
    void givenDriverId_whenGetTripsByDriver_thenReturnsOnlyDriverTrips() throws Exception {
        tripRepository.save(buildTripEntity("Dakar", "Rufisque", 1500.0, DRIVER_ID, "veh-1"));
        tripRepository.save(buildTripEntity("Dakar", "Pikine", 1000.0, DRIVER_ID, "veh-2"));
        tripRepository.save(buildTripEntity("Dakar", "Thies", 5000.0, "other-driver", "veh-3"));

        mockMvc.perform(get("/api/trips/driver/" + DRIVER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    // =========================================================================
    // Tests de mise à jour de statut
    // =========================================================================

    @Test
    @DisplayName("PATCH /api/trips/{id}/status - Met à jour le statut du trajet")
    void givenExistingTrip_whenUpdateStatus_thenReturnsUpdatedStatus() throws Exception {
        // Le trajet doit appartenir au même driver que le SecurityContext (BOLA)
        Trajet saved = tripRepository.save(buildTripEntity("Dakar", "Thies", 5000.0, DRIVER_ID, "veh-Z"));

        mockMvc.perform(patch("/api/trips/" + saved.getId() + "/status")
                .param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.statutTrajet", is("IN_PROGRESS")));
    }

    @Test
    @DisplayName("PATCH /api/trips/{id}/status - Statut invalide retourne 400")
    void givenInvalidStatus_whenUpdateStatus_thenReturns400() throws Exception {
        Trajet saved = tripRepository.save(buildTripEntity("Dakar", "Kaolack", 4000.0, DRIVER_ID, "veh-Y"));

        mockMvc.perform(patch("/api/trips/" + saved.getId() + "/status")
                .param("status", "STATUT_INEXISTANT"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Tests de décrémentation des places
    // =========================================================================

    @Test
    @DisplayName("POST /api/trips/{id}/decrement-seats - Décrémente les places disponibles")
    void givenExistingTripWithSeats_whenDecrementSeats_thenUpdatesAvailableSeats() throws Exception {
        // Le trajet doit appartenir au même driver (BOLA)
        Trajet saved = tripRepository.save(buildTripEntity("Dakar", "Thies", 5000.0, DRIVER_ID, "veh-W"));

        mockMvc.perform(post("/api/trips/" + saved.getId() + "/decrement-seats")
                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placesDisponibles", is(2)));
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================

    /**
     * Configure le SecurityContext avec un utilisateur fictif identifié par userId.
     */
    static void mockSecurityContext(String userId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CreateTripRequest buildCreateTripRequest(String depart, String arrivee, double prix,
            int places, String driverId, String vehicleId) {
        CreateTripRequest req = new CreateTripRequest();
        req.setDepart(depart);
        req.setArrivee(arrivee);
        req.setPrix(prix);
        req.setPlacesDisponibles(places);
        req.setDriverId(driverId);
        req.setVehicleId(vehicleId);
        req.setDateDepart(LocalDateTime.now().plusHours(2));
        return req;
    }

    private Trajet buildTripEntity(String depart, String arrivee, double prix,
            String driverId, String vehicleId) {
        Trajet t = new Trajet();
        t.setDepart(depart);
        t.setArrivee(arrivee);
        t.setPrix(prix);
        t.setPlacesDisponibles(4);
        t.setDriverId(driverId);
        t.setVehicleId(vehicleId);
        t.setDateDepart(LocalDateTime.now().plusHours(2));
        t.setStatutTrajet(StatutTrajet.CREATED);
        return t;
    }
}
