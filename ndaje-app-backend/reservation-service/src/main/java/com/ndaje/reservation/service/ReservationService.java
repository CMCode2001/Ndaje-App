package com.ndaje.reservation.service;

import com.ndaje.reservation.dto.request.CreateReservationRequest;
import com.ndaje.reservation.dto.request.UpdateReservationStatusRequest;
import com.ndaje.reservation.dto.response.ReservationResponse;
import com.ndaje.reservation.dto.response.TripAvailabilityResponse;
import com.ndaje.reservation.entity.StatutReservation;

import java.util.List;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);
    ReservationResponse getReservationById(Long id);
    List<ReservationResponse> getReservationsByPassager(Long passagerId);
    ReservationResponse updateReservationStatus(Long id, StatutReservation status);
    List<TripAvailabilityResponse> getAvailableTrips();
}
