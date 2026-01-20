package com.ndaje.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {

    @NotNull(message = "Trip ID cannot be null")
    private Long tripId;

    @NotNull(message = "Passager ID cannot be null")
    private Long passagerId;
}
