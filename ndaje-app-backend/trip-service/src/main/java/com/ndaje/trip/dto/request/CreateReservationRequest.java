package com.ndaje.trip.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotBlank(message = "L'ID du passager est obligatoire")
    private String passengerId;

    @NotNull(message = "L'ID du voyage (trajet ou caravane) est obligatoire")
    private Long voyageId;

    @NotBlank(message = "Le type de voyage (TRAJET ou CARAVANE) est obligatoire")
    private String typeVoyage;

    @Min(value = 1, message = "Le nombre de places doit être au moins 1")
    private int places;
}
