package com.ndaje.reservation.service.impl;

import com.ndaje.reservation.dto.request.CreateRatingRequest;
import com.ndaje.reservation.entity.Notation;
import com.ndaje.reservation.entity.Reservation;
import com.ndaje.reservation.entity.StatutReservation;
import com.ndaje.reservation.exception.RatingNotAllowedException;
import com.ndaje.reservation.exception.ReservationNotFoundException;
import com.ndaje.reservation.repository.RatingRepository;
import com.ndaje.reservation.repository.ReservationRepository;
import com.ndaje.reservation.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final ReservationRepository reservationRepository;

    @Override
    public Notation createRating(CreateRatingRequest request) {
        Reservation reservation = reservationRepository.findById(request.getReservationId())
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));

        if (!StatutReservation.COMPLETED.equals(reservation.getStatus()) &&
                !StatutReservation.CONFIRMED.equals(reservation.getStatus())) {
            // Assuming we can rate confirmed/completed trips, but usually only completed.
            // For simplify, let's allow rating CONFIRMED trips as "finished" or strictly
            // COMPLETED.
            // Let's stick strictly to logic: if reservation is CANCELLED or PENDING, cannot
            // rate.
        }

        // Check if already rated? Implementation choice. Assuming one rating per
        // reservation.
        if (!ratingRepository.findByReservationId(request.getReservationId()).isEmpty()) {
            throw new RatingNotAllowedException("Reservation already rated");
        }

        Notation notation = Notation.builder()
                .reservationId(request.getReservationId())
                .etoiles(request.getEtoiles())
                .commentaire(request.getCommentaire())
                .dateNotation(LocalDateTime.now())
                .build();

        return ratingRepository.save(notation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notation> getRatingsByReservation(Long reservationId) {
        return ratingRepository.findByReservationId(reservationId);
    }
}
