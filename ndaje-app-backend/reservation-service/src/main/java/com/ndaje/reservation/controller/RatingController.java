package com.ndaje.reservation.controller;

import com.ndaje.reservation.dto.request.CreateRatingRequest;
import com.ndaje.reservation.dto.response.ApiResponse;
import com.ndaje.reservation.entity.Notation;
import com.ndaje.reservation.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Notation>> createRating(@Valid @RequestBody CreateRatingRequest request) {
        Notation notation = ratingService.createRating(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<Notation>builder()
                        .success(true)
                        .message("Rating submitted successfully")
                        .data(notation)
                        .build());
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<ApiResponse<List<Notation>>> getRatingsByReservation(@PathVariable Long reservationId) {
        List<Notation> notations = ratingService.getRatingsByReservation(reservationId);
        return ResponseEntity.ok()
                .body(ApiResponse.<List<Notation>>builder()
                        .success(true)
                        .message("Ratings retrieved successfully")
                        .data(notations)
                        .build());
    }
}
