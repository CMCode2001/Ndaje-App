package com.ndaje.reservation.client;

import com.ndaje.reservation.dto.TripResponse;
import com.ndaje.reservation.dto.ApiResponse; // Need to create ApiResponse DTO or handle generic Object
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "trip-service", url = "${application.config.trip-service-url}")
public interface TripClient {

    @GetMapping("/api/trips/{id}")
    ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable("id") Long id); // Returns ApiResponse wrapped

    // Actually Feign can decode ApiResponse<TripResponse> directly if configured,
    // or I can define a wrapper DTO.
    // Let's create `ApiResponse` DTO in reservation service to match common
    // structure.

    @PostMapping("/api/trips/{id}/decrement-seats")
    ResponseEntity<ApiResponse<TripResponse>> decrementSeats(@PathVariable("id") Long id,
            @RequestParam("quantity") int quantity);

    @PostMapping("/api/trips/{id}/increment-seats")
    ResponseEntity<ApiResponse<TripResponse>> incrementSeats(@PathVariable("id") Long id,
            @RequestParam("quantity") int quantity);

    @GetMapping("/api/trips/driver/{driverId}")
    ResponseEntity<ApiResponse<java.util.List<TripResponse>>> getTripsByDriverId(@PathVariable("driverId") String driverId);
}
