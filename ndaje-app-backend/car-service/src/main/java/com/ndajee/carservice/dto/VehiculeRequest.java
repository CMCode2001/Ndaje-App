package com.ndajee.carservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeRequest {
    private String marque;
    private String modele;
    private String immatriculation;
    private String couleur;
    private int annee;
    private int places;
    private String driverId;
}
