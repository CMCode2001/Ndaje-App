package com.ndaje.trip.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Notation d'un trajet par un passager après la course.
 * Appartient sémantiquement au domaine Trip (on note un trajet/conducteur),
 * et non au domaine Réservation (contrat commercial).
 */
@Entity
@Table(name = "notations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** ID de la réservation qui est à l'origine de la notation */
    @Column(nullable = false)
    private Long reservationId;

    /** ID du trajet noté */
    @Column(nullable = false)
    private Long trajetId;

    /** ID du passager qui note */
    @Column(nullable = false)
    private String passagerId;

    /** Note de 1 à 5 étoiles */
    @Column(nullable = false)
    private int etoiles;

    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateNotation;

    @PrePersist
    protected void onCreate() {
        this.dateNotation = LocalDateTime.now();
    }
}
