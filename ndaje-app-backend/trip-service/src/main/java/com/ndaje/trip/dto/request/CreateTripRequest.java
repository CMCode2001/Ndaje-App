package com.ndaje.trip.dto.request;

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
public class CreateTripRequest {

    private String driverId;

    private String vehicleId;

    @NotBlank(message = "Depart cannot be blank")
    private String depart;

    @NotBlank(message = "Arrivee cannot be blank")
    private String arrivee;

    @NotNull(message = "Date depart cannot be null")
    @Future(message = "Date depart must be in the future")
    private LocalDateTime dateDepart;

    @Min(value = 1, message = "Places disponibles must be at least 1")
    private int placesDisponibles;

    @Min(value = 0, message = "Prix cannot be negative")
    private double prix;
}
