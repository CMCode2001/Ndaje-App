package com.ndaje.trip.service.impl;

import com.ndaje.trip.client.UserClient;
import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.ReservationResponse;
import com.ndaje.trip.entity.Caravane;
import com.ndaje.trip.entity.Reservation;
import com.ndaje.trip.entity.StatutReservation;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.repository.CaravaneRepository;
import com.ndaje.trip.repository.ReservationRepository;
import com.ndaje.trip.repository.TripRepository;
import com.ndaje.trip.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final TripRepository tripRepository;
    private final CaravaneRepository caravaneRepository;
    private final UserClient userClient;

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // 1. Validation du passager
        try {
            userClient.getUserById(request.getPassengerId());
        } catch (Exception e) {
            throw new BusinessException("Passager invalide ou User-Service inaccessible");
        }

        // 2. Traitement selon le type de voyage (TRAJET vs CARAVANE)
        String type = request.getTypeVoyage().toUpperCase();

        if ("TRAJET".equals(type)) {
            Trajet trajet = tripRepository.findById(request.getVoyageId())
                    .orElseThrow(() -> new BusinessException("Trajet introuvable: " + request.getVoyageId()));

            if (trajet.getPlacesDisponibles() < request.getPlaces()) {
                throw new BusinessException("Pas assez de places disponibles sur ce trajet");
            }

            // Décrémenter les places sur le Trajet
            trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - request.getPlaces());
            tripRepository.save(trajet);

        } else if ("CARAVANE".equals(type)) {
            Caravane caravane = caravaneRepository.findById(request.getVoyageId())
                    .orElseThrow(() -> new BusinessException("Caravane introuvable: " + request.getVoyageId()));

            if (caravane.getPlacesDisponibles() < request.getPlaces()) {
                throw new BusinessException("Pas assez de places disponibles sur cette caravane");
            }

            // Décrémenter les places sur la Caravane
            caravane.setPlacesDisponibles(caravane.getPlacesDisponibles() - request.getPlaces());
            caravaneRepository.save(caravane);

        } else {
            throw new BusinessException("Type de voyage inconnu: " + type + " (Attendu: TRAJET ou CARAVANE)");
        }

        // 3. Création de la réservation (Dans la même transaction SQL !)
        Reservation reservation = Reservation.builder()
                .passengerId(request.getPassengerId())
                .voyageId(request.getVoyageId())
                .typeVoyage(type)
                .places(request.getPlaces())
                .reservationDate(LocalDateTime.now())
                .status(StatutReservation.CONFIRMED)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        log.info("Réservation {} créée pour le passager {} sur le {} {}",
                saved.getId(), saved.getPassengerId(), type, saved.getVoyageId());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPassengerId(String passengerId) {
        return reservationRepository.findByPassengerId(passengerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelReservation(Long reservationId, String passengerId) {
        Reservation res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("Réservation introuvable"));

        if (!res.getPassengerId().equals(passengerId)) {
            throw new BusinessException("Non autorisé à annuler cette réservation");
        }

        if (res.getStatus() == StatutReservation.CANCELLED) {
            throw new BusinessException("La réservation est déjà annulée");
        }

        // Restituer les places
        String type = res.getTypeVoyage();
        if ("TRAJET".equals(type)) {
            tripRepository.findById(res.getVoyageId()).ifPresent(t -> {
                t.setPlacesDisponibles(t.getPlacesDisponibles() + res.getPlaces());
                tripRepository.save(t);
            });
        } else if ("CARAVANE".equals(type)) {
            caravaneRepository.findById(res.getVoyageId()).ifPresent(c -> {
                c.setPlacesDisponibles(c.getPlacesDisponibles() + res.getPlaces());
                caravaneRepository.save(c);
            });
        }

        // Mettre à jour le statut
        res.setStatus(StatutReservation.CANCELLED);
        reservationRepository.save(res);
        log.info("Réservation {} annulée et {} places restituées au {} {}",
                reservationId, res.getPlaces(), type, res.getVoyageId());
    }

    private ReservationResponse toResponse(Reservation r) {
        return ReservationResponse.builder()
                .id(r.getId())
                .passengerId(r.getPassengerId())
                .voyageId(r.getVoyageId())
                .typeVoyage(r.getTypeVoyage())
                .places(r.getPlaces())
                .reservationDate(r.getReservationDate())
                .status(r.getStatus().name())
                .build();
    }
}
