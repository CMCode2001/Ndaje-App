package com.ndaje.trip.controller;

import com.ndaje.trip.dto.request.CreateCaravaneRequest;
import com.ndaje.trip.dto.response.ApiResponse;
import com.ndaje.trip.dto.response.CaravaneResponse;
import com.ndaje.trip.service.CaravaneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caravanes")
@RequiredArgsConstructor
public class CaravaneController {

    private final CaravaneService caravaneService;

    /**
     * Création d'une nouvelle caravane (pour le profil CARAVANNIER).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CaravaneResponse>> createCaravane(
            @Valid @RequestBody CreateCaravaneRequest request) {

        CaravaneResponse caravane = caravaneService.createCaravane(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CaravaneResponse>builder()
                        .success(true)
                        .message("Caravane " + request.getNom() + " organisée avec succès")
                        .data(caravane)
                        .build());
    }

    /**
     * Lit les infos détaillées d'une caravane spécifique.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaravaneResponse>> getCaravaneById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ApiResponse.<CaravaneResponse>builder()
                .success(true)
                .message("Détails de la caravane")
                .data(caravaneService.getCaravaneById(id))
                .build());
    }

    /**
     * Affiche le "tableau de bord" des voyages collectifs ouverts.
     * Pour les utilisateurs cherchant des caravanes.
     */
    @GetMapping("/ouvertes")
    public ResponseEntity<ApiResponse<List<CaravaneResponse>>> getCaravanesOuvertes() {
        return ResponseEntity.ok(ApiResponse.<List<CaravaneResponse>>builder()
                .success(true)
                .message("Liste des caravanes ouvertes")
                .data(caravaneService.getCaravanesOuvertes())
                .build());
    }

    /**
     * Permet à un caravannier de voir tout son historique et ses caravanes en
     * cours.
     */
    @GetMapping("/caravannier/{id}")
    public ResponseEntity<ApiResponse<List<CaravaneResponse>>> getCaravanesByCaravannier(
            @PathVariable("id") String caravannierId) {

        return ResponseEntity.ok(ApiResponse.<List<CaravaneResponse>>builder()
                .success(true)
                .message("Vos caravanes")
                .data(caravaneService.getCaravanesByCaravannier(caravannierId))
                .build());
    }

    /**
     * Recherche textuelle rapide par ville d'arrivée/départ.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CaravaneResponse>>> searchCaravanes(
            @RequestParam("ville") String ville) {

        return ResponseEntity.ok(ApiResponse.<List<CaravaneResponse>>builder()
                .success(true)
                .message("Résultat de recherche pour : " + ville)
                .data(caravaneService.searchCaravanes(ville))
                .build());
    }

    /**
     * Annule la caravane si c'est possible.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelCaravane(
            @PathVariable Long id,
            @RequestParam("caravannierId") String caravannierId) {

        caravaneService.cancelCaravane(id, caravannierId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Caravane annulée avec succès")
                .build());
    }
}
