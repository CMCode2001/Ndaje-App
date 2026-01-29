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
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerPhone;
    private Long tripId;
    private String depart;
    private String arrivee;
    private LocalDateTime dateDepart;
    private LocalDateTime reservationDate;
    private int places;
    private StatutReservation status;
}
