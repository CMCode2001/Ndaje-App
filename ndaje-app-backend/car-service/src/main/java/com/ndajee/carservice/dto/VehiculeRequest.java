package com.ndajee.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeRequest {

    @NotBlank(message = "La marque est obligatoire")
    @Size(min = 2, max = 50, message = "La marque doit faire entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9 -]+$", message = "La marque contient des caractères invalides")
    private String marque;

    @NotBlank(message = "Le modèle est obligatoire")
    @Size(min = 2, max = 50, message = "Le modèle doit faire entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9 -]+$", message = "Le modèle contient des caractères invalides")
    private String modele;

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Size(min = 4, max = 20, message = "L'immatriculation doit faire entre 4 et 20 caractères")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "L'immatriculation doit contenir uniquement des lettres majuscules, chiffres et tirets")
    private String immatriculation;

    @NotBlank(message = "La couleur est obligatoire")
    @Size(min = 3, max = 30, message = "La couleur doit faire entre 3 et 30 caractères")
    @Pattern(regexp = "^[a-zA-Z -]+$", message = "La couleur contient des caractères invalides")
    private String couleur;

    private int annee;
    private int places;
    private String driverId;
}
