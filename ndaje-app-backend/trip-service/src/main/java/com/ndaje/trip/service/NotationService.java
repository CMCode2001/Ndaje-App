package com.ndaje.trip.service;

import com.ndaje.trip.dto.request.CreateNotationRequest;
import com.ndaje.trip.entity.Notation;

import java.util.List;

public interface NotationService {

    Notation createNotation(CreateNotationRequest request);

    List<Notation> getNotationsByTrajet(Long trajetId);

    List<Notation> getNotationsByReservation(Long reservationId);

    double getMoyenneParTrajet(Long trajetId);
}
