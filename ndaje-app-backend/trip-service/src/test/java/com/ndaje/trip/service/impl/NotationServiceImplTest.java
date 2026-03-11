package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateNotationRequest;
import com.ndaje.trip.entity.Notation;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.TripNotFoundException;
import com.ndaje.trip.repository.NotationRepository;
import com.ndaje.trip.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotationServiceImplTest {

    @Mock
    private NotationRepository notationRepository;

    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private NotationServiceImpl notationService;

    private CreateNotationRequest request;
    private Notation notation;
    private Trajet trajet;

    @BeforeEach
    void setUp() {
        request = new CreateNotationRequest(1L, 10L, "passenger-1", 5, "Très bon trajet !");

        notation = Notation.builder()
                .id(100L)
                .reservationId(1L)
                .trajetId(10L)
                .passagerId("passenger-1")
                .etoiles(5)
                .commentaire("Très bon trajet !")
                .build();

        trajet = Trajet.builder()
                .id(10L)
                .build();
    }

    @Test
    void createNotation_ShouldCreateSuccessfully() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trajet));
        when(notationRepository.existsByReservationIdAndPassagerId(anyLong(), anyString())).thenReturn(false);
        when(notationRepository.save(any(Notation.class))).thenReturn(notation);

        Notation response = notationService.createNotation(request);

        assertNotNull(response);
        assertEquals(5, response.getEtoiles());
        verify(notationRepository, times(1)).save(any(Notation.class));
    }

    @Test
    void createNotation_ShouldThrowException_WhenTripNotFound() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(TripNotFoundException.class, () -> notationService.createNotation(request));
        verify(notationRepository, never()).save(any(Notation.class));
    }

    @Test
    void createNotation_ShouldThrowException_WhenAlreadyNoted() {
        when(tripRepository.findById(anyLong())).thenReturn(Optional.of(trajet));
        when(notationRepository.existsByReservationIdAndPassagerId(anyLong(), anyString())).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> notationService.createNotation(request));
        verify(notationRepository, never()).save(any(Notation.class));
    }

    @Test
    void getNotationsByTrajet_ShouldReturnList() {
        when(notationRepository.findByTrajetId(10L)).thenReturn(List.of(notation));

        List<Notation> notations = notationService.getNotationsByTrajet(10L);

        assertFalse(notations.isEmpty());
        assertEquals(1, notations.size());
    }

    @Test
    void getNotationsByReservation_ShouldReturnList() {
        when(notationRepository.findByReservationId(1L)).thenReturn(List.of(notation));

        List<Notation> notations = notationService.getNotationsByReservation(1L);

        assertFalse(notations.isEmpty());
        assertEquals(1, notations.size());
    }

    @Test
    void getMoyenneParTrajet_ShouldReturnAverage() {
        Notation notation2 = Notation.builder()
                .etoiles(3)
                .build();

        when(notationRepository.findByTrajetId(10L)).thenReturn(List.of(notation, notation2)); // 5 and 3

        double avg = notationService.getMoyenneParTrajet(10L);

        assertEquals(4.0, avg);
    }

    @Test
    void getMoyenneParTrajet_ShouldReturnZeroWhenEmpty() {
        when(notationRepository.findByTrajetId(10L)).thenReturn(List.of());

        double avg = notationService.getMoyenneParTrajet(10L);

        assertEquals(0.0, avg);
    }
}
