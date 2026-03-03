package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateCaravaneRequest;
import com.ndaje.trip.dto.response.CaravaneResponse;
import com.ndaje.trip.entity.Caravane;
import com.ndaje.trip.entity.StatutCaravane;
import com.ndaje.trip.exception.BusinessException;
import com.ndaje.trip.repository.CaravaneRepository;
import com.ndaje.trip.service.CaravaneService;
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
public class CaravaneServiceImpl implements CaravaneService {

    private final CaravaneRepository caravaneRepository;

    @Override
    public CaravaneResponse createCaravane(CreateCaravaneRequest request) {
        if (request.getDateArriveeEstimee() != null &&
                request.getDateArriveeEstimee().isBefore(request.getDateDepart())) {
            throw new BusinessException("La date d'arrivée ne peut pas être antérieure au départ.");
        }

        Caravane caravane = Caravane.builder()
                .caravannierI(request.getCaravannierId())
                .nom(request.getNom())
                .description(request.getDescription())
                .depart(request.getDepart())
                .arrivee(request.getArrivee())
                .etapes(request.getEtapes())
                .dateDepart(request.getDateDepart())
                .dateArriveeEstimee(request.getDateArriveeEstimee())
                .maxParticipants(request.getMaxParticipants())
                // Places disponibles sera initialisé à maxParticipants par @PrePersist
                .prixParPersonne(request.getPrixParPersonne())
                .vehiculeIds(request.getVehiculeIds())
                .theme(request.getTheme())
                // Statut sera initialisé à OUVERTE par @PrePersist
                .build();

        Caravane saved = caravaneRepository.save(caravane);
        log.info("Caravane '{}' créée par {} avec {} places",
                saved.getNom(), saved.getCaravannierI(), saved.getMaxParticipants());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CaravaneResponse getCaravaneById(Long id) {
        Caravane caravane = caravaneRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Caravane introuvable: " + id));
        return toResponse(caravane);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaravaneResponse> getCaravanesOuvertes() {
        // Renvoie uniquement les caravanes qui ont encore de la place et qui n'ont pas
        // encore démarré
        return caravaneRepository.findByStatutAndPlacesDisponiblesGreaterThanAndDateDepartAfter(
                StatutCaravane.OUVERTE, 0, LocalDateTime.now())
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaravaneResponse> getCaravanesByCaravannier(String caravannierId) {
        return caravaneRepository.findByCaravannierId(caravannierId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CaravaneResponse> searchCaravanes(String ville) {
        return caravaneRepository.findByDepartIgnoreCaseOrArriveeIgnoreCase(ville, ville)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public void cancelCaravane(Long id, String caravannierId) {
        Caravane caravane = caravaneRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Caravane introuvable: " + id));

        if (!caravane.getCaravannierI().equals(caravannierId)) {
            throw new BusinessException("Vous n'êtes pas autorisé à annuler cette caravane.");
        }

        if (caravane.getStatut() == StatutCaravane.EN_COURS || caravane.getStatut() == StatutCaravane.TERMINEE) {
            throw new BusinessException("Impossible d'annuler une caravane déjà en cours ou terminée.");
        }

        caravane.setStatut(StatutCaravane.ANNULEE);
        caravaneRepository.save(caravane);
        log.info("Caravane id={} annulée par le caravannier {}", id, caravannierId);
    }

    // Mapper interne
    private CaravaneResponse toResponse(Caravane c) {
        return CaravaneResponse.builder()
                .id(c.getId())
                .caravannierId(c.getCaravannierI())
                .nom(c.getNom())
                .description(c.getDescription())
                .depart(c.getDepart())
                .arrivee(c.getArrivee())
                .etapes(c.getEtapes())
                .dateDepart(c.getDateDepart())
                .dateArriveeEstimee(c.getDateArriveeEstimee())
                .maxParticipants(c.getMaxParticipants())
                .placesDisponibles(c.getPlacesDisponibles())
                .prixParPersonne(c.getPrixParPersonne())
                .vehiculeIds(c.getVehiculeIds())
                .theme(c.getTheme() != null ? c.getTheme().name() : null)
                .statut(c.getStatut().name())
                .dateCreation(c.getDateCreation())
                .build();
    }
}
