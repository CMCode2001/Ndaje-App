package com.ndaje.trip.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaravaneResponse {
    private Long id;
    private String caravannierId;
    private String nom;
    private String description;
    private String depart;
    private String arrivee;
    private String etapes;
    private LocalDateTime dateDepart;
    private LocalDateTime dateArriveeEstimee;
    private int maxParticipants;
    private int placesDisponibles;
    private double prixParPersonne;
    private String vehiculeIds;
    private String theme;
    private String statut;
    private LocalDateTime dateCreation;
}
