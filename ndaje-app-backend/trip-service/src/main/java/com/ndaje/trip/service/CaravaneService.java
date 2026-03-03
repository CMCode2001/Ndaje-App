package com.ndaje.trip.service;

import com.ndaje.trip.dto.request.CreateCaravaneRequest;
import com.ndaje.trip.dto.response.CaravaneResponse;

import java.util.List;

public interface CaravaneService {

    CaravaneResponse createCaravane(CreateCaravaneRequest request);

    CaravaneResponse getCaravaneById(Long id);

    List<CaravaneResponse> getCaravanesOuvertes();

    List<CaravaneResponse> getCaravanesByCaravannier(String caravannierId);

    List<CaravaneResponse> searchCaravanes(String ville);

    void cancelCaravane(Long id, String caravannierId); // Security check
}
