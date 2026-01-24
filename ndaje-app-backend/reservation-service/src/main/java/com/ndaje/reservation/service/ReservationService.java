package com.ndaje.reservation.service;

import com.ndaje.reservation.dto.CreateReservationRequest;
import com.ndaje.reservation.dto.ReservationResponse;
import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    List<ReservationResponse> getReservationsByPassengerId(String passengerId);
    // Could add cancelReservation, etc.
}
