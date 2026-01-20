package com.ndaje.trip.controller;

import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.ApiResponse;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    @PostMapping
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@Valid @RequestBody CreateTripRequest request) {
        TripResponse tripResponse = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TripResponse>builder()
                        .success(true)
                        .message("Trip created successfully")
                        .data(tripResponse)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable Long id) {
        TripResponse tripResponse = tripService.getTripById(id);
        return ResponseEntity.ok()
                .body(ApiResponse.<TripResponse>builder()
                        .success(true)
                        .message("Trip retrieved successfully")
                        .data(tripResponse)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TripResponse>>> getAllTrips() {
        List<TripResponse> trips = tripService.getAllTrips();
        return ResponseEntity.ok()
                .body(ApiResponse.<List<TripResponse>>builder()
                        .success(true)
                        .message("Trips retrieved successfully")
                        .data(trips)
                        .build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TripResponse>> updateTripStatus(
            @PathVariable Long id,
            @RequestParam StatutTrajet status) {
        TripResponse tripResponse = tripService.updateTripStatus(id, status);
        return ResponseEntity.ok()
                .body(ApiResponse.<TripResponse>builder()
                        .success(true)
                        .message("Trip status updated successfully")
                        .data(tripResponse)
                        .build());
    }

    @PostMapping("/{id}/decrement-seats")
    public ResponseEntity<ApiResponse<TripResponse>> decrementSeats(
            @PathVariable Long id,
            @RequestParam int quantity) {
        TripResponse tripResponse = tripService.decrementSeats(id, quantity);
        return ResponseEntity.ok()
                .body(ApiResponse.<TripResponse>builder()
                        .success(true)
                        .message("Seats decremented successfully")
                        .data(tripResponse)
                        .build());
    }
}
