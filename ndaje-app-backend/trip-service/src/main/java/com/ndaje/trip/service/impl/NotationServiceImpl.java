package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateNotationRequest;
import com.ndaje.trip.entity.Notation;
import com.ndaje.trip.exception.TripNotFoundException;
import com.ndaje.trip.repository.NotationRepository;
import com.ndaje.trip.repository.TripRepository;
import com.ndaje.trip.service.NotationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotationServiceImpl implements NotationService {

    private final NotationRepository notationRepository;
    private final TripRepository tripRepository;

    @Override
    public Notation createNotation(CreateNotationRequest request) {
        // Vérifie que le trajet existe
        tripRepository.findById(request.getTrajetId())
                .orElseThrow(() -> new TripNotFoundException("Trajet introuvable: " + request.getTrajetId()));

        // Un passager ne peut noter qu'une seule fois par réservation
        if (notationRepository.existsByReservationIdAndPassagerId(
                request.getReservationId(), request.getPassagerId())) {
            throw new IllegalStateException("Ce passager a déjà noté cette réservation");
        }

        Notation notation = Notation.builder()
                .reservationId(request.getReservationId())
                .trajetId(request.getTrajetId())
                .passagerId(request.getPassagerId())
                .etoiles(request.getEtoiles())
                .commentaire(request.getCommentaire())
                .build();

        Notation saved = notationRepository.save(notation);
        log.info("Notation créée: trajet={}, passager={}, etoiles={}",
                request.getTrajetId(), request.getPassagerId(), request.getEtoiles());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notation> getNotationsByTrajet(Long trajetId) {
        return notationRepository.findByTrajetId(trajetId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notation> getNotationsByReservation(Long reservationId) {
        return notationRepository.findByReservationId(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public double getMoyenneParTrajet(Long trajetId) {
        List<Notation> notations = notationRepository.findByTrajetId(trajetId);
        if (notations.isEmpty())
            return 0.0;
        return notations.stream()
                .mapToInt(Notation::getEtoiles)
                .average()
                .orElse(0.0);
    }
}
