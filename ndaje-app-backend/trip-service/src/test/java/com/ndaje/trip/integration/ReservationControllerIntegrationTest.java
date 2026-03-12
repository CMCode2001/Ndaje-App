package com.ndaje.trip.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ndaje.trip.client.CarClient;
import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.UserDto;
import com.ndaje.trip.entity.*;
import com.ndaje.trip.repository.CaravaneRepository;
import com.ndaje.trip.repository.ReservationRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration du ReservationController.
 * Couvre les deux types de voyages : TRAJET et CARAVANE.
 * Vérifie que la sécurité BOLA est correctement appliquée.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("integration")
@DisplayName("ReservationController - Tests d'intégration (Trajets & Caravanes)")
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private CaravaneRepository caravaneRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @MockBean
    private CarClient carClient;

    @MockBean
    private UserClient userClient;

    private Trajet activeTrip;
    private Caravane activeCaravane;

    @BeforeEach
    void setUp() {
        reservationRepository.deleteAll();
        caravaneRepository.deleteAll();
        tripRepository.deleteAll();

        // --- Trajet disponible ---
        Trajet trip = new Trajet();
        trip.setDepart("Dakar");
        trip.setArrivee("Kaolack");
        trip.setPrix(4500.0);
        trip.setPlacesDisponibles(4);
        trip.setDriverId("driver-001");
        trip.setVehicleId("vehicle-001");
        trip.setDateDepart(LocalDateTime.now().plusHours(3));
        trip.setStatutTrajet(StatutTrajet.CREATED);
        activeTrip = tripRepository.save(trip);

        // --- Caravane disponible ---
        Caravane caravane = new Caravane();
        caravane.setCaravannierId("caravannier-001");
        caravane.setNom("Caravane Dakar-Touba 2026");
        caravane.setDescription("Caravane annuelle du grand Magal");
        caravane.setDepart("Dakar");
        caravane.setArrivee("Touba");
        caravane.setPrixParPersonne(7500.0);
        caravane.setPlacesDisponibles(20);
        caravane.setMaxParticipants(20);
        caravane.setDateDepart(LocalDateTime.now().plusDays(5));
        caravane.setDateArriveeEstimee(LocalDateTime.now().plusDays(6));
        caravane.setStatut(StatutCaravane.OUVERTE);
        caravane.setTheme(ThemeCaravane.RELIGIEUX);
        activeCaravane = caravaneRepository.save(caravane);

        // --- Stubs Feign ---
        UserDto fakeUser = new UserDto();
        fakeUser.setId("passenger-001");
        fakeUser.setEmail("pass@test.com");
        fakeUser.setRole("PASSENGER");
        when(userClient.getUserById(anyString())).thenReturn(fakeUser);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // =========================================================================
    // BLOC 1 : Réservation d'un TRAJET
    // =========================================================================

    @Test
    @DisplayName("[TRAJET] Réservation réussie → retourne 201 with passenger-001")
    void givenValidTrajetRequest_whenCreate_thenReturns201WithPassengerInfo() throws Exception {
        String passengerId = "passenger-001";
        mockSecurityContext(passengerId); // BOLA check: passengerId must match current user

        CreateReservationRequest request = buildRequest(activeTrip.getId(), "TRAJET", passengerId, 2);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.passengerId", is(passengerId)))
                .andExpect(jsonPath("$.data.typeVoyage", is("TRAJET")));
    }

    @Test
    @DisplayName("[TRAJET] Réservation avec plus de places que disponibles → retourne 400")
    void givenTooManySeats_forTrajet_thenReturns400() throws Exception {
        mockSecurityContext("passenger-002");
        CreateReservationRequest request = buildRequest(activeTrip.getId(), "TRAJET", "passenger-002", 10);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Pas assez de places")));
    }

    @Test
    @DisplayName("[TRAJET] Trajet inexistant → retourne 400 (BusinessException) via Handler")
    void givenNonExistentTripId_thenReturns404() throws Exception {
        mockSecurityContext("passenger-004");
        CreateReservationRequest request = buildRequest(99999L, "TRAJET", "passenger-004", 1);

        // Note: GlobalExceptionHandler maps BusinessException to 400 Bad Request
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // BLOC 2 : Réservation d'une CARAVANE
    // =========================================================================

    @Test
    @DisplayName("[CARAVANE] Réservation réussie → retourne 201")
    void givenValidCaravaneRequest_whenCreate_thenReturns201WithCaravaneInfo() throws Exception {
        mockSecurityContext("passenger-005");
        CreateReservationRequest request = buildRequest(activeCaravane.getId(), "CARAVANE", "passenger-005", 3);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.typeVoyage", is("CARAVANE")))
                .andExpect(jsonPath("$.data.passengerId", is("passenger-005")));
    }

    @Test
    @DisplayName("[CARAVANE] Réservation qui remplit toutes les places → succès")
    void givenFullCaravaneReservation_whenCreate_thenSucceedsAndFillsAllSeats() throws Exception {
        mockSecurityContext("passenger-006");
        CreateReservationRequest request = buildRequest(activeCaravane.getId(), "CARAVANE", "passenger-006", 20);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("[CARAVANE] Caravane inexistante → retourne 400 (BusinessException)")
    void givenNonExistentCaravaneId_thenReturns404() throws Exception {
        mockSecurityContext("passenger-008");
        CreateReservationRequest request = buildRequest(88888L, "CARAVANE", "passenger-008", 1);

        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // BLOC 3 : Historique des réservations
    // =========================================================================

    @Test
    @DisplayName("GET /passenger/{id} - Retourne l'historique complet d'un passager")
    void givenPassengerWithMixedReservations_whenGetHistory_thenReturnsAll() throws Exception {
        saveReservationInDb("mixed-passenger", activeTrip.getId(), "TRAJET", 1);
        saveReservationInDb("mixed-passenger", activeCaravane.getId(), "CARAVANE", 2);

        mockMvc.perform(get("/api/reservations/passenger/mixed-passenger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[*].typeVoyage", containsInAnyOrder("TRAJET", "CARAVANE")));
    }

    // =========================================================================
    // BLOC 4 : Annulation (Sécurité BOLA)
    // =========================================================================

    @Test
    @DisplayName("[ANNULATION TRAJET] Le propriétaire peut annuler sa réservation")
    void givenOwner_whenCancelTrajetReservation_thenReturns200() throws Exception {
        Reservation res = saveReservationInDb("owner-passenger", activeTrip.getId(), "TRAJET", 1);
        mockSecurityContext("owner-passenger");

        mockMvc.perform(delete("/api/reservations/" + res.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @DisplayName("[SÉCURITÉ BOLA] Un tiers ne peut PAS annuler la réservation d'un tiers → retourne 400 (BusinessException)")
    void givenDifferentUser_whenCancelOtherReservation_thenReturns403() throws Exception {
        Reservation res = saveReservationInDb("real-owner", activeTrip.getId(), "TRAJET", 1);
        mockSecurityContext("hacker");

        // Actuellement BusinessException retourne 400 via GlobalExceptionHandler
        mockMvc.perform(delete("/api/reservations/" + res.getId()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("[ANNULATION] ID de réservation inexistant → retourne 400 (BusinessException)")
    void givenNonExistentReservationId_whenDelete_thenReturns404() throws Exception {
        mockSecurityContext("any-passenger");

        mockMvc.perform(delete("/api/reservations/99999"))
                .andExpect(status().isBadRequest());
    }

    // =========================================================================
    // Méthodes utilitaires
    // =========================================================================

    private void mockSecurityContext(String userId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private CreateReservationRequest buildRequest(Long voyageId, String type, String passengerId, int places) {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setVoyageId(voyageId);
        req.setTypeVoyage(type);
        req.setPassengerId(passengerId);
        req.setPlaces(places);
        return req;
    }

    private Reservation saveReservationInDb(String passengerId, Long voyageId, String type, int places) {
        Reservation r = Reservation.builder()
                .passengerId(passengerId)
                .voyageId(voyageId)
                .typeVoyage(type)
                .places(places)
                .reservationDate(LocalDateTime.now())
                .status(StatutReservation.CONFIRMED)
                .build();
        return reservationRepository.save(r);
    }
}
