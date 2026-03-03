package com.ndaje.trip.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "trajets")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Trajet extends AbstractVoyage {

    @Column(nullable = false)
    private String driverId; // Changed from Long conducteurId to String for Keycloak integration

    @Column(nullable = false)
    private String vehicleId;

    private double prix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTrajet statutTrajet;
}
