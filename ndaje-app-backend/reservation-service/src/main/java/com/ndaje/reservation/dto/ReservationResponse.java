package com.ndaje.reservation.dto;

import com.ndaje.reservation.entity.StatutReservation;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationResponse {
    private Long id;
    private String passengerId;
    private Long tripId;
    private LocalDateTime reservationDate;
    private int places;
    private StatutReservation status;
}
