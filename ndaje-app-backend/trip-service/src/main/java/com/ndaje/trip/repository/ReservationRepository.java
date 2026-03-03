package com.ndaje.trip.repository;

import com.ndaje.trip.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByPassengerId(String passengerId);

    List<Reservation> findByVoyageIdAndTypeVoyage(Long voyageId, String typeVoyage);
}
