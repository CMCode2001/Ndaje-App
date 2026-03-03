package com.ndaje.trip.dto.response;

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
    private String passengerId;
    private Long voyageId;
    private String typeVoyage;
    private int places;
    private LocalDateTime reservationDate;
    private String status;
}
