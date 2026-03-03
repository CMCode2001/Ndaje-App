package com.ndaje.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String passengerId;

    /** ID du voyage (Trajet ou Caravane) */
    @Column(nullable = false)
    private Long voyageId;

    /**
     * Type de voyage réservé:
     * "TRAJET" pour les trajets classiques, "CARAVANE" pour les caravanes
     */
    @Column(nullable = false)
    private String typeVoyage;

    @Column(nullable = false)
    private LocalDateTime reservationDate;

    private int places;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReservation status;
}
