package com.ndaje.reservation.service;

import com.ndaje.reservation.dto.CreateReservationRequest;
import com.ndaje.reservation.dto.ReservationResponse;
import com.ndaje.reservation.dto.UpdateReservationRequest;
import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);

    List<ReservationResponse> getReservationsByPassengerId(String passengerId);

    void cancelReservation(Long id);

    ReservationResponse updateReservation(Long id, UpdateReservationRequest request);

    List<ReservationResponse> getReservationsByDriverId(String driverId);
}
