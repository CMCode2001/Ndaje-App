package com.ndaje.trip.repository;

import com.ndaje.trip.entity.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trajet, Long> {
    java.util.List<Trajet> findByDriverId(String driverId);
}
