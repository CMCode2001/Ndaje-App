package com.ndaje.trip.service;

import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.entity.StatutTrajet;

import java.util.List;

public interface TripService {
    TripResponse createTrip(CreateTripRequest request);

    TripResponse getTripById(Long id);

    List<TripResponse> getAllTrips();
    
    TripResponse updateTripStatus(Long id, StatutTrajet status);
    
    TripResponse decrementSeats(Long id, int quantity);
}
