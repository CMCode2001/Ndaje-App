package com.ndaje.reservation.service.impl;

import com.ndaje.reservation.client.TripServiceClient;
import com.ndaje.reservation.dto.request.CreateReservationRequest;
import com.ndaje.reservation.dto.response.ApiResponse;
import com.ndaje.reservation.dto.response.ReservationResponse;
import com.ndaje.reservation.dto.response.TripAvailabilityResponse;
import com.ndaje.reservation.entity.Reservation;
import com.ndaje.reservation.entity.StatutReservation;
import com.ndaje.reservation.exception.ReservationNotFoundException;
import com.ndaje.reservation.exception.TripFullException;
import com.ndaje.reservation.repository.ReservationRepository;
import com.ndaje.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
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
    private final TripServiceClient tripServiceClient;

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        // 1. Try to decrement seats in TripService
        // Assuming 1 seat per reservation for now
        int seatsToBook = 1; 
        
        try {
            ApiResponse<TripAvailabilityResponse> decrementResponse = tripServiceClient.decrementSeats(request.getTripId(), seatsToBook);
            
            if (decrementResponse == null || !decrementResponse.isSuccess()) {
                throw new TripFullException("Reservation pleine pour ce trajet");
            }
        } catch (Exception e) {
             // If we get an error from the client (e.g. 400 Bad Request because seats are not enough), 
             // we assume it is because the trip is full.
             throw new TripFullException("Reservation pleine pour ce trajet");
        }

        // 2. Create reservation if decrement was successful
        Reservation reservation = Reservation.builder()
                .tripId(request.getTripId())
                .passagerId(request.getPassagerId())
                .dateReservation(LocalDateTime.now())
                .statutReservation(StatutReservation.CONFIRMED)
                .build();

        Reservation savedReservation = reservationRepository.save(reservation);

        return mapToReservationResponse(savedReservation);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));
        return mapToReservationResponse(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByPassager(Long passagerId) {
        return reservationRepository.findByPassagerId(passagerId).stream()
                .map(this::mapToReservationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ReservationResponse updateReservationStatus(Long id, StatutReservation status) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found with id: " + id));
        reservation.setStatutReservation(status);
        Reservation updatedReservation = reservationRepository.save(reservation);
        return mapToReservationResponse(updatedReservation);
    }

    @Override
    public List<TripAvailabilityResponse> getAvailableTrips() {
        ApiResponse<List<TripAvailabilityResponse>> response = tripServiceClient.getAllTrips();
        if (response != null && response.isSuccess()) {
             // Filter trips with available seats > 0
            return response.getData().stream()
                    .filter(t -> t.getPlacesDisponibles() > 0)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private ReservationResponse mapToReservationResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .tripId(reservation.getTripId())
                .passagerId(reservation.getPassagerId())
                .dateReservation(reservation.getDateReservation())
                .statutReservation(reservation.getStatutReservation())
                .build();
    }
}
