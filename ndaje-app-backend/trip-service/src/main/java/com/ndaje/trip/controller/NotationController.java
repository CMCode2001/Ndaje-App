package com.ndaje.trip.controller;

import com.ndaje.trip.dto.request.CreateNotationRequest;
import com.ndaje.trip.dto.response.ApiResponse;
import com.ndaje.trip.entity.Notation;
import com.ndaje.trip.service.NotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de notation des trajets.
 * Anciennement dans reservation-service — déplacé ici car sémantiquement
 * une notation concerne un trajet (et son conducteur), pas une réservation.
 */
@RestController
@RequestMapping("/api/notations")
@RequiredArgsConstructor
public class NotationController {

    private final NotationService notationService;

    /** Soumettre une notation pour un trajet effectué */
    @PostMapping
    public ResponseEntity<ApiResponse<Notation>> createNotation(
            @Valid @RequestBody CreateNotationRequest request) {
        Notation notation = notationService.createNotation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Notation>builder()
                        .success(true)
                        .message("Notation soumise avec succès")
                        .data(notation)
                        .build());
    }

    /** Récupérer toutes les notations d'un trajet */
    @GetMapping("/trajet/{trajetId}")
    public ResponseEntity<ApiResponse<List<Notation>>> getNotationsByTrajet(
            @PathVariable Long trajetId) {
        List<Notation> notations = notationService.getNotationsByTrajet(trajetId);
        return ResponseEntity.ok(ApiResponse.<List<Notation>>builder()
                .success(true)
                .message("Notations du trajet récupérées")
                .data(notations)
                .build());
    }

    /** Récupérer les notations liées à une réservation */
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<List<Notation>>> getNotationsByReservation(
            @PathVariable Long reservationId) {
        List<Notation> notations = notationService.getNotationsByReservation(reservationId);
        return ResponseEntity.ok(ApiResponse.<List<Notation>>builder()
                .success(true)
                .message("Notations de la réservation récupérées")
                .data(notations)
                .build());
    }

    /**
     * Note moyenne d'un trajet (utile pour afficher la réputation d'un conducteur)
     */
    @GetMapping("/trajet/{trajetId}/moyenne")
    public ResponseEntity<ApiResponse<Double>> getMoyenneTrajet(
            @PathVariable Long trajetId) {
        double moyenne = notationService.getMoyenneParTrajet(trajetId);
        return ResponseEntity.ok(ApiResponse.<Double>builder()
                .success(true)
                .message("Moyenne calculée")
                .data(moyenne)
                .build());
    }
}
