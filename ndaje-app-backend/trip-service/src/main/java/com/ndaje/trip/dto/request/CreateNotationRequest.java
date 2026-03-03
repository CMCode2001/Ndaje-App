package com.ndaje.trip.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateNotationRequest {

    @NotNull(message = "L'ID de la réservation est obligatoire")
    private Long reservationId;

    @NotNull(message = "L'ID du trajet est obligatoire")
    private Long trajetId;

    @NotBlank(message = "L'ID du passager est obligatoire")
    private String passagerId;

    @Min(value = 1, message = "La note doit être au minimum 1")
    @Max(value = 5, message = "La note doit être au maximum 5")
    private int etoiles;

    private String commentaire;
}
