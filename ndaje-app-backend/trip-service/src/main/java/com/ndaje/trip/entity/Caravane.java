package com.ndaje.trip.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Caravane : voyage collectif organisé par un Caravannier.
 * Différences clés avec un Trajet classique :
 * - Créé par un CARAVANNIER (et non un conducteur)
 * - Plusieurs véhicules impliqués (flotte)
 * - Thème/description de la caravane
 * - Peut avoir des étapes intermédiaires
 * - Date d'arrivée estimée en plus de la date de départ
 * - Nombre max de participants (passagers toutes voitures confondues)
 */
@Entity
@Table(name = "caravanes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Caravane extends AbstractVoyage {

    /** Identifiant Keycloak du caravannier organisateur */
    @Column(nullable = false)
    private String caravannierI;

    /** Nom/titre de la caravane (ex: "Caravane Dakar-Tambacounda") */
    @Column(nullable = false)
    private String nom;

    /** Description détaillée de la caravane (programme, règles...) */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Étapes intermédiaires (liste de villes séparées par des virgules) */
    private String etapes;

    /** Date et heure d'arrivée estimée */
    private LocalDateTime dateArriveeEstimee;

    /** Nombre maximum de participants acceptés */
    @Column(nullable = false)
    private int maxParticipants;

    /** Prix par participant */
    private double prixParPersonne;

    /** Véhicules participants à la caravane (IDs séparés par virgule) */
    @Column(columnDefinition = "TEXT")
    private String vehiculeIds;

    /**
     * Thème de la caravane (voyage touristique, pèlerinage, événement sportif...)
     */
    @Enumerated(EnumType.STRING)
    private ThemeCaravane theme;

    /** Statut courant de la caravane */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCaravane statut;

    /** Date de création de l'annonce */
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        if (this.statut == null)
            this.statut = StatutCaravane.OUVERTE;
        if (this.getPlacesDisponibles() == 0)
            this.setPlacesDisponibles(this.getMaxParticipants());
    }
}
