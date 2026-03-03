package com.ndaje.trip.repository;

import com.ndaje.trip.entity.Notation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotationRepository extends JpaRepository<Notation, Long> {

    List<Notation> findByTrajetId(Long trajetId);

    List<Notation> findByReservationId(Long reservationId);

    boolean existsByReservationIdAndPassagerId(Long reservationId, String passagerId);
}
