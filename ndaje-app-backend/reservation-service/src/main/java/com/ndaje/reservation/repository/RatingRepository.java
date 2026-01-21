package com.ndaje.reservation.repository;

import com.ndaje.reservation.entity.Notation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Notation, Long> {
    List<Notation> findByReservationId(Long reservationId);
}
