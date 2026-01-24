package com.ndajee.carservice.repository;

import com.ndajee.carservice.domain.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, Long> {
    java.util.List<Vehicule> findByDriverId(String driverId);
}
