package com.ndaje.trip.service;

import com.ndaje.trip.dto.request.CreateReservationRequest;
import com.ndaje.trip.dto.response.ReservationResponse;

import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    List<ReservationResponse> getReservationsByPassengerId(String passengerId);

    void cancelReservation(Long reservationId);
}
