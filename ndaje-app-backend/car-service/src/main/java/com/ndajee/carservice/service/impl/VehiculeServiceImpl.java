package com.ndajee.carservice.service.impl;

import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.repository.VehiculeRepository;
import com.ndajee.carservice.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    @Override
    public Vehicule createVehicule(Vehicule vehicule) {
        return vehiculeRepository.save(vehicule);
    }

    @Override
    public Vehicule updateVehicule(Long id, Vehicule vehicule) {
        return vehiculeRepository.findById(id)
                .map(existingVehicule -> {
                    existingVehicule.setMarque(vehicule.getMarque());
                    existingVehicule.setModele(vehicule.getModele());
                    existingVehicule.setImmatriculation(vehicule.getImmatriculation());
                    existingVehicule.setCouleur(vehicule.getCouleur());
                    return vehiculeRepository.save(existingVehicule);
                })
                .orElseThrow(() -> new RuntimeException("Vehicule not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicule> getVehiculeById(Long id) {
        return vehiculeRepository.findById(id);
    }

    @Override
    public void deleteVehicule(Long id) {
        vehiculeRepository.deleteById(id);
    }
}
