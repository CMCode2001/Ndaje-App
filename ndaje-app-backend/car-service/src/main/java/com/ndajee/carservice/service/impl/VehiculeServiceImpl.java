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
        if (vehicule.getDriverId() == null || vehicule.getDriverId().isBlank()) {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtToken) {
                vehicule.setDriverId(jwtToken.getName());
            }
        }

        if (vehicule.getDriverId() == null || vehicule.getDriverId().isBlank()) {
            throw new RuntimeException("Driver ID is required and could not be determined from security context");
        }

        // Initialisation du statut de vérification
        vehicule.setStatutVerification(com.ndajee.carservice.domain.StatutVerificationVehicule.EN_ATTENTE);

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
                    existingVehicule.setStatutVerification(vehicule.getStatutVerification());
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
    @Transactional(readOnly = true)
    public List<Vehicule> getVehiculesByDriverId(String driverId) {
        return vehiculeRepository.findByDriverId(driverId);
    }

    @Override
    public void deleteVehicule(Long id) {
        vehiculeRepository.deleteById(id);
    }
}
