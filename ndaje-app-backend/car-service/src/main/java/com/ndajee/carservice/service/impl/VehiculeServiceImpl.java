package com.ndajee.carservice.service.impl;

import com.ndajee.carservice.client.DocumentClient;
import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import com.ndajee.carservice.mapper.VehiculeMapper;
import com.ndajee.carservice.repository.VehiculeRepository;
import com.ndajee.carservice.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import com.ndajee.carservice.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final DocumentClient documentClient;
    private final VehiculeMapper vehiculeMapper;

    @Override
    public VehiculeResponse createVehicule(VehiculeRequest vehiculeRequest) {
        Vehicule vehicule = vehiculeMapper.toEntity(vehiculeRequest);

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

        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse updateVehicule(Long id, VehiculeRequest vehiculeRequest) {
        return vehiculeRepository.findById(id)
                .map(existingVehicule -> {
                    vehiculeMapper.updateEntityFromRequest(vehiculeRequest, existingVehicule);
                    Vehicule saved = vehiculeRepository.save(existingVehicule);
                    return vehiculeMapper.toResponse(saved);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getAllVehicules() {
        return vehiculeMapper.toResponseList(vehiculeRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VehiculeResponse> getVehiculeById(Long id) {
        return vehiculeRepository.findById(id).map(vehiculeMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculeResponse> getVehiculesByDriverId(String driverId) {
        return vehiculeMapper.toResponseList(vehiculeRepository.findByDriverId(driverId));
    }

    @Override
    public void deleteVehicule(Long id) {
        vehiculeRepository.deleteById(id);
    }

    @Override
    public DocumentResponse uploadDocument(Long vehiculeId, MultipartFile file, String typeDocument, String numero,
            String expiration) {
        vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule not found with id: " + vehiculeId));

        return documentClient.uploadDocument(file, String.valueOf(vehiculeId), "VEHICLE", typeDocument, numero,
                expiration);
    }

    @Override
    public List<DocumentResponse> getVehiculeDocuments(Long vehiculeId) {
        if (!vehiculeRepository.existsById(vehiculeId)) {
            throw new ResourceNotFoundException("Vehicule not found with id: " + vehiculeId);
        }
        return documentClient.getDocumentsByEntity(String.valueOf(vehiculeId), "VEHICLE");
    }
}
