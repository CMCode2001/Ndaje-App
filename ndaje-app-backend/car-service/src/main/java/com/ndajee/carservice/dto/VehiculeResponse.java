package com.ndajee.carservice.dto;

import com.ndajee.carservice.domain.StatutVerificationVehicule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeResponse {
    private Long id;
    private String marque;
    private String modele;
    private String immatriculation;
    private String couleur;
    private int annee;
    private int places;
    private StatutVerificationVehicule statutVerification;
    private String driverId;
}
