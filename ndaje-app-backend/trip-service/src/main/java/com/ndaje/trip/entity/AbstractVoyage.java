package com.ndaje.trip.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Classe de base commune contenant les informations principales
 * d'un déplacement d'un point A à un point B.
 */
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class AbstractVoyage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String depart;

    @Column(nullable = false)
    private String arrivee;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    private int placesDisponibles;
}
