package com.ndaje.reservation.service.impl;

import com.ndaje.reservation.client.TripClient;
import com.ndaje.reservation.client.UserClient;
import com.ndaje.reservation.dto.ApiResponse;
import com.ndaje.reservation.dto.CreateReservationRequest;
import com.ndaje.reservation.dto.ReservationResponse;
import com.ndaje.reservation.dto.TripResponse;
import com.ndaje.reservation.entity.Reservation;
import com.ndaje.reservation.entity.StatutReservation;
import com.ndaje.reservation.exception.BusinessException;
import com.ndaje.reservation.repository.ReservationRepository;
import com.ndaje.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserClient userClient;
    private final TripClient tripClient;

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // 1. Validate Passenger
        try {
            userClient.getUserById(request.getPassengerId());
        } catch (Exception e) {
            throw new BusinessException("Invalid Passenger ID"); // Or UserService unavailable
        }

        // 2. Validate Trip and Check Availability via TripService
        ResponseEntity<ApiResponse<TripResponse>> tripResponseWrapper;
        try {
            tripResponseWrapper = tripClient.getTripById(request.getTripId());
        } catch (Exception e) {
            throw new BusinessException("Invalid Trip ID or Trip Service unavailable");
        }

        if (tripResponseWrapper == null || tripResponseWrapper.getBody() == null
                || !tripResponseWrapper.getBody().isSuccess()) {
            throw new BusinessException("Trip not found");
        }

        TripResponse trip = tripResponseWrapper.getBody().getData();

        if (trip.getPlacesDisponibles() < request.getPlaces()) {
            throw new BusinessException("Not enough seats available");
        }

        // 3. Decrement Seats in TripService
        // Ideally this should be a distributed transaction (Saga), but for now we call
        // immediately.
        // If save fails later, we might have decremented seats without booking.
        // Better: decrement seats first, if success, save reservation. If save fails,
        // compensate (increment).
        // For simplicity: Call decrement. If it fails, it throws.

        try {
            tripClient.decrementSeats(request.getTripId(), request.getPlaces());
        } catch (Exception e) {
            throw new BusinessException("Failed to book seats in Trip Service");
        }

        // 4. Save Reservation
        Reservation reservation = Reservation.builder()
                .passengerId(request.getPassengerId())
                .tripId(request.getTripId())
                .places(request.getPlaces())
                .reservationDate(LocalDateTime.now())
                .status(StatutReservation.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);
        return mapToResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPassengerId(String passengerId) {
        return reservationRepository.findByPassengerId(passengerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .passengerId(reservation.getPassengerId())
                .tripId(reservation.getTripId())
                .places(reservation.getPlaces())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus())
                .build();
    }
}
