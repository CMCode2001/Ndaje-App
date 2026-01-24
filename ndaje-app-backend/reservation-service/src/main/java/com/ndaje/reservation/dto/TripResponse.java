package com.ndaje.reservation.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TripResponse {
    private Long id;
    private String driverId;
    private String vehicleId;
    private String depart;
    private String arrivee;
    private LocalDateTime dateDepart;
    private int placesDisponibles;
    private double prix;
    private String statutTrajet;
}
