package com.ndaje.reservation.dto.response;

import com.ndaje.reservation.entity.StatutReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private Long tripId;
    private Long passagerId;
    private LocalDateTime dateReservation;
    private StatutReservation statutReservation;
}
