package com.ndaje.reservation.repository;

import com.ndaje.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPassengerId(String passengerId);

    List<Reservation> findByTripId(Long tripId);

    List<Reservation> findByTripIdIn(List<Long> tripIds);
}
