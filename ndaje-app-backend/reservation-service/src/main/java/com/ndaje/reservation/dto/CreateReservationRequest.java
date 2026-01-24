package com.ndaje.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReservationRequest {

    @NotNull(message = "Passenger ID cannot be null")
    private String passengerId;

    @NotNull(message = "Trip ID cannot be null")
    private Long tripId;

    @Min(value = 1, message = "Places must be at least 1")
    private int places;
}
