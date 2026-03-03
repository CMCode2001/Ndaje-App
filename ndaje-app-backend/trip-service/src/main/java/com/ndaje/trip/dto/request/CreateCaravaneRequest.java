package com.ndaje.trip.dto.request;

import com.ndaje.trip.entity.ThemeCaravane;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaravaneRequest {

    @NotBlank(message = "L'ID du caravannier est obligatoire")
    private String caravannierId;

    @NotBlank(message = "Le nom de la caravane est obligatoire")
    private String nom;

    private String description;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    private String depart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    private String arrivee;

    private String etapes; // Optionnel : ex "Thiès, Diourbel"

    @NotNull(message = "La date de départ est obligatoire")
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDateTime dateDepart;

    @Future(message = "La date d'arrivée estimée doit être dans le futur")
    private LocalDateTime dateArriveeEstimee;

    @Min(value = 1, message = "Il doit y avoir au moins 1 participant")
    private int maxParticipants;

    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    private double prixParPersonne;

    private String vehiculeIds; // Optionnel à la création

    private ThemeCaravane theme;
}
