package com.ndaje.trip.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTripRequest {

    private String driverId;

    private String vehicleId;

    @NotBlank(message = "Depart cannot be blank")
    @Size(min = 2, max = 100, message = "Le lieu de départ doit faire entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 -]+$", message = "Le départ contient des caractères invalides")
    private String depart;

    @NotBlank(message = "Arrivee cannot be blank")
    @Size(min = 2, max = 100, message = "Le lieu d'arrivée doit faire entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 -]+$", message = "L'arrivée contient des caractères invalides")
    private String arrivee;

    @NotNull(message = "Date depart cannot be null")
    @Future(message = "Date depart must be in the future")
    private LocalDateTime dateDepart;

    @Min(value = 1, message = "Places disponibles must be at least 1")
    private int placesDisponibles;

    @Min(value = 0, message = "Prix cannot be negative")
    private double prix;
}
