package com.ndaje.reservation.service;

import com.ndaje.reservation.dto.request.CreateRatingRequest;
import com.ndaje.reservation.entity.Notation;

import java.util.List;

public interface RatingService {
    Notation createRating(CreateRatingRequest request);
    List<Notation> getRatingsByReservation(Long reservationId);
}
