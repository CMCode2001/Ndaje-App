package com.ndaje.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripAvailabilityResponse {
    private Long id;
    private Long conducteurId;
    private String depart;
    private String arrivee;
    private LocalDateTime dateDepart;
    private int placesDisponibles;
    private double prix;
}
