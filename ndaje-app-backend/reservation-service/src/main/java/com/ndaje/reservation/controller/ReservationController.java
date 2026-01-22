package com.ndaje.reservation.controller;

import com.ndaje.reservation.dto.request.CreateReservationRequest;
import com.ndaje.reservation.dto.request.UpdateReservationStatusRequest;
import com.ndaje.reservation.dto.response.ApiResponse;
import com.ndaje.reservation.dto.response.ReservationResponse;
import com.ndaje.reservation.dto.response.TripAvailabilityResponse;
import com.ndaje.reservation.entity.StatutReservation;
import com.ndaje.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/trips/available")
    public ResponseEntity<ApiResponse<List<TripAvailabilityResponse>>> getAvailableTrips() {
        List<TripAvailabilityResponse> trips = reservationService.getAvailableTrips();
        return ResponseEntity.ok()
                .body(ApiResponse.<List<TripAvailabilityResponse>>builder()
                        .success(true)
                        .message("Available trips retrieved successfully")
                        .data(trips)
                        .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ReservationResponse>builder()
                        .success(true)
                        .message("Reservation created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(@PathVariable Long id) {
        ReservationResponse response = reservationService.getReservationById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.<ReservationResponse>builder()
                        .success(true)
                        .message("Reservation retrieved successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByPassager(@RequestParam Long passagerId) {
        List<ReservationResponse> response = reservationService.getReservationsByPassager(passagerId);
        return ResponseEntity.ok()
                .body(ApiResponse.<List<ReservationResponse>>builder()
                        .success(true)
                        .message("Reservations retrieved successfully")
                        .data(response)
                        .build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationStatusRequest request) {
        ReservationResponse response = reservationService.updateReservationStatus(id, request.getStatus());
        return ResponseEntity.ok()
                .body(ApiResponse.<ReservationResponse>builder()
                        .success(true)
                        .message("Reservation status updated successfully")
                        .data(response)
                        .build());
    }
}
