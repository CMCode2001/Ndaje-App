package com.ndaje.reservation.dto.request;

import com.ndaje.reservation.entity.StatutReservation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationStatusRequest {
    @NotNull(message = "Status cannot be null")
    private StatutReservation status;
}
