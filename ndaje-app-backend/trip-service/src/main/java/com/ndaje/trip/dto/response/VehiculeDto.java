package com.ndaje.trip.dto.response;

import lombok.Data;

@Data
public class VehiculeDto {
    private Long id;
    private String driverId;
    private String marque;
    private String modele;
    private String immatriculation;
    private int places;
}
