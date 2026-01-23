package com.ndajee.carservice.service;

import com.ndajee.carservice.domain.Vehicule;
import java.util.List;
import java.util.Optional;

public interface VehiculeService {
    Vehicule createVehicule(Vehicule vehicule);
    Vehicule updateVehicule(Long id, Vehicule vehicule);
    List<Vehicule> getAllVehicules();
    Optional<Vehicule> getVehiculeById(Long id);
    void deleteVehicule(Long id);
}
