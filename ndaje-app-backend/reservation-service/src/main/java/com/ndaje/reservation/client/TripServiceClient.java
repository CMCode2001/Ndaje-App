package com.ndaje.reservation.client;

import com.ndaje.reservation.dto.response.ApiResponse;
import com.ndaje.reservation.dto.response.TripAvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "trip-service")
public interface TripServiceClient {

    @GetMapping("/api/trips/{id}")
    ApiResponse<TripAvailabilityResponse> getTripById(@PathVariable("id") Long id);

    @GetMapping("/api/trips")
    ApiResponse<List<TripAvailabilityResponse>> getAllTrips();

    @PostMapping("/api/trips/{id}/decrement-seats")
    ApiResponse<TripAvailabilityResponse> decrementSeats(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}
