package com.ndaje.trip.controller;

import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.ApiResponse;
import com.ndaje.trip.dto.response.ReservationResponse;
import com.ndaje.trip.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller gérant les réservations de trajets et de caravanes.
 * Migré depuis reservation-service vers trip-service pour garantir l'atomicité
 * (décrémenter les places et sauver la réservation dans la même
 * base/transaction).
 */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

        private final ReservationService reservationService;

        /**
         * Crée une réservation ("TRAJET" ou "CARAVANE") et décrémente les places en
         * temps réel.
         */
        @PostMapping
        public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
                        @Valid @RequestBody CreateReservationRequest request) {

                ReservationResponse response = reservationService.createReservation(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ReservationResponse>builder()
                                                .success(true)
                                                .message("Réservation confirmée avec succès")
                                                .data(response)
                                                .build());
        }

        /**
         * Historique des réservations d'un passager.
         */
        @GetMapping("/passenger/{passengerId}")
        public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByPassengerId(
                        @PathVariable String passengerId) {

                List<ReservationResponse> reservations = reservationService.getReservationsByPassengerId(passengerId);

                return ResponseEntity.ok(ApiResponse.<List<ReservationResponse>>builder()
                                .success(true)
                                .message("Historique des réservations récupéré")
                                .data(reservations)
                                .build());
        }

        /**
         * Annule une réservation (si le passager en est bien l'auteur).
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> cancelReservation(
                        @PathVariable("id") Long id) {

                reservationService.cancelReservation(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Réservation annulée, places restituées")
                                .build());
        }
}
