package com.ndaje.reservation.service.impl;

import com.ndaje.reservation.client.TripClient;
import com.ndaje.reservation.client.UserClient;
import com.ndaje.reservation.dto.ApiResponse;
import com.ndaje.reservation.dto.CreateReservationRequest;
import com.ndaje.reservation.dto.ReservationResponse;
import com.ndaje.reservation.dto.TripResponse;
import com.ndaje.reservation.dto.UpdateReservationRequest;
import com.ndaje.reservation.dto.UserDto;
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
            throw new BusinessException("Invalid Passenger ID");
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
        return mapToResponse(savedReservation, trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPassengerId(String passengerId) {
        List<Reservation> reservations = reservationRepository.findByPassengerId(passengerId);
        java.util.Map<Long, TripResponse> tripCache = new java.util.HashMap<>();

        return reservations.stream()
                .map(reservation -> {
                    TripResponse trip = tripCache.computeIfAbsent(reservation.getTripId(), id -> {
                        try {
                            ResponseEntity<ApiResponse<TripResponse>> response = tripClient.getTripById(id);
                            if (response != null && response.getBody() != null && response.getBody().isSuccess()) {
                                return response.getBody().getData();
                            }
                        } catch (Exception e) {
                        }
                        return null;
                    });
                    return mapToResponse(reservation, trip);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reservation not found with id: " + id));

        if (reservation.getStatus() == StatutReservation.CANCELLED) {
            throw new BusinessException("Reservation is already cancelled");
        }

        // 1. Update status
        reservation.setStatus(StatutReservation.CANCELLED);
        reservationRepository.save(reservation);

        // 2. Increment seats back in Trip Service
        try {
            tripClient.incrementSeats(reservation.getTripId(), reservation.getPlaces());
        } catch (Exception e) {
            throw new BusinessException("Failed to update seats in Trip Service during cancellation");
        }
    }

    @Override
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Reservation not found with id: " + id));

        if (reservation.getStatus() == StatutReservation.CANCELLED) {
            throw new BusinessException("Cannot update a cancelled reservation");
        }

        int oldPlaces = reservation.getPlaces();
        int newPlaces = request.getPlaces();
        int diff = newPlaces - oldPlaces;

        if (diff != 0) {
            try {
                if (diff > 0) {
                    tripClient.decrementSeats(reservation.getTripId(), diff);
                } else {
                    tripClient.incrementSeats(reservation.getTripId(), Math.abs(diff));
                }
            } catch (Exception e) {
                throw new BusinessException("Failed to adjust seats in Trip Service: " + e.getMessage());
            }
        }

        reservation.setPlaces(newPlaces);
        Reservation updatedReservation = reservationRepository.save(reservation);

        TripResponse trip = null;
        try {
            ResponseEntity<ApiResponse<TripResponse>> response = tripClient.getTripById(updatedReservation.getTripId());
            if (response != null && response.getBody() != null && response.getBody().isSuccess()) {
                trip = response.getBody().getData();
            }
        } catch (Exception e) {
        }

        return mapToResponse(updatedReservation, trip);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByDriverId(String driverId) {
        // 1. Get driver's trips
        ResponseEntity<ApiResponse<List<TripResponse>>> tripsWrapper = tripClient.getTripsByDriverId(driverId);
        if (tripsWrapper == null || tripsWrapper.getBody() == null || !tripsWrapper.getBody().isSuccess()) {
            return java.util.Collections.emptyList();
        }

        List<TripResponse> trips = tripsWrapper.getBody().getData();
        if (trips == null || trips.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        java.util.Map<Long, TripResponse> tripMap = trips.stream()
                .collect(Collectors.toMap(TripResponse::getId, t -> t));
        List<Long> tripIds = new java.util.ArrayList<>(tripMap.keySet());

        // 2. Get reservations for these trips
        List<Reservation> reservations = reservationRepository.findByTripIdIn(tripIds);
        
        // 3. Enrich and map
        java.util.Map<String, UserDto> passengerCache = new java.util.HashMap<>();
        return reservations.stream()
                .map(reservation -> {
                    TripResponse trip = tripMap.get(reservation.getTripId());
                    UserDto passenger = passengerCache.computeIfAbsent(reservation.getPassengerId(), id -> {
                        try {
                            return userClient.getUserById(id);
                        } catch (Exception e) {
                            return null;
                        }
                    });
                    return mapToResponse(reservation, trip, passenger);
                })
                .collect(Collectors.toList());
    }

    private ReservationResponse mapToResponse(Reservation reservation, TripResponse trip, UserDto passenger) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .passengerId(reservation.getPassengerId())
                .passengerFirstName(passenger != null ? passenger.getPrenom() : null)
                .passengerLastName(passenger != null ? passenger.getNom() : null)
                .passengerPhone(passenger != null ? passenger.getTelephone() : null)
                .tripId(reservation.getTripId())
                .depart(trip != null ? trip.getDepart() : null)
                .arrivee(trip != null ? trip.getArrivee() : null)
                .dateDepart(trip != null ? trip.getDateDepart() : null)
                .places(reservation.getPlaces())
                .reservationDate(reservation.getReservationDate())
                .status(reservation.getStatus())
                .build();
    }

    private ReservationResponse mapToResponse(Reservation reservation, TripResponse trip) {
        return mapToResponse(reservation, trip, null);
    }
}
