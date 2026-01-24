package com.ndaje.trip.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateTripRequest {

    private String depart;

    private String arrivee;

    @Future(message = "Date depart must be in the future")
    private LocalDateTime dateDepart;

    @Min(value = 1, message = "Places disponibles must be at least 1")
    private Integer placesDisponibles;

    @Min(value = 0, message = "Prix cannot be negative")
    private Double prix;

    // We generally don't update driverId or vehicleId for an existing trip, but
    // could if needed.
    // For now, let's keep it simple.
}
