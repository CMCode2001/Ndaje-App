package com.ndaje.trip.dto.response;

import com.ndaje.trip.entity.StatutTrajet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripResponse {
    private Long id;
    private String driverId;
    private String vehicleId;
    private String depart;
    private String arrivee;
    private LocalDateTime dateDepart;
    private int placesDisponibles;
    private double prix;
    private StatutTrajet statutTrajet;
}
