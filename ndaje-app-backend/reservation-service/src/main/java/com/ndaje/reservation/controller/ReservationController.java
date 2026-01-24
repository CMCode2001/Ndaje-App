package com.ndaje.reservation.controller;

import com.ndaje.reservation.dto.ApiResponse;
import com.ndaje.reservation.dto.CreateReservationRequest;
import com.ndaje.reservation.dto.ReservationResponse;
import com.ndaje.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

        private final ReservationService reservationService;

        @PostMapping
        public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
                        @Valid @RequestBody CreateReservationRequest request) {
                ReservationResponse response = reservationService.createReservation(request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<ReservationResponse>builder()
                                                .success(true)
                                                .message("Reservation created successfully")
                                                .data(response)
                                                .build());
        }

        @GetMapping("/passenger/{passengerId}")
        public ResponseEntity<ApiResponse<List<ReservationResponse>>> getReservationsByPassenger(
                        @PathVariable String passengerId) {
                List<ReservationResponse> responses = reservationService.getReservationsByPassengerId(passengerId);
                return ResponseEntity.ok()
                                .body(ApiResponse.<List<ReservationResponse>>builder()
                                                .success(true)
                                                .message("Reservations retrieved successfully")
                                                .data(responses)
                                                .build());
        }
}
