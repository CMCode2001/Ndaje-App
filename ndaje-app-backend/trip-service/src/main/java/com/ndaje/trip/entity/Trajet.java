package com.ndaje.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trajets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trajet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String driverId; // Changed from Long conducteurId to String for Keycloak integration

    @Column(nullable = false)
    private String vehicleId;

    @Column(nullable = false)
    private String depart;

    @Column(nullable = false)
    private String arrivee;

    @Column(nullable = false)
    private LocalDateTime dateDepart;

    private int placesDisponibles;

    private double prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statutTrajet;
}
