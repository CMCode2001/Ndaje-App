package com.ndajee.carservice.service.impl;

import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import com.ndajee.carservice.exception.BusinessException;
import com.ndajee.carservice.exception.ResourceNotFoundException;
import com.ndajee.carservice.mapper.VehiculeMapper;
import com.ndajee.carservice.repository.VehiculeRepository;
import com.ndajee.carservice.service.VehiculeDocumentService;
import com.ndajee.carservice.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ndajee.carservice.security.SecurityUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@lombok.extern.slf4j.Slf4j
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final VehiculeMapper vehiculeMapper;
    private final VehiculeDocumentService vehiculeDocumentService; // ← local, plus de Feign

    @Override
    public VehiculeResponse createVehicule(VehiculeRequest vehiculeRequest) {
        Vehicule vehicule = vehiculeMapper.toEntity(vehiculeRequest);

        if (vehicule.getDriverId() == null || vehicule.getDriverId().isBlank()) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication instanceof JwtAuthenticationToken jwtToken) {
                vehicule.setDriverId(jwtToken.getName());
            }
        }

        if (vehicule.getDriverId() == null || vehicule.getDriverId().isBlank()) {
            throw new BusinessException("Driver ID is required and could not be determined from security context");
        }

        vehicule.setStatutVerification(com.ndajee.carservice.domain.StatutVerificationVehicule.EN_ATTENTE);
        return vehiculeMapper.toResponse(vehiculeRepository.save(vehicule));
    }

    @Override
    public VehiculeResponse updateVehicule(Long id, VehiculeRequest vehiculeRequest) {
        return vehiculeRepository.findById(id)
                .map(existingVehicule -> {
                    // Enforce BOLA/IDOR protection
                    SecurityUtils.verifyOwnership(existingVehicule.getDriverId());

                    vehiculeMapper.updateEntityFromRequest(vehiculeRequest, existingVehicule);
                    return vehiculeMapper.toResponse(vehiculeRepository.save(existingVehicule));
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
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule not found with id: " + id));
        // Enforce BOLA/IDOR protection
        SecurityUtils.verifyOwnership(vehicule.getDriverId());
        vehiculeRepository.deleteById(id);
    }

    @Override
    public DocumentResponse uploadDocument(Long vehiculeId, MultipartFile file,
            String typeDocument, String numero, String expiration) {
        // Vérifie que le véhicule existe
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule not found with id: " + vehiculeId));

        // Enforce BOLA/IDOR protection
        SecurityUtils.verifyOwnership(vehicule.getDriverId());

        // Délègue au service local — plus de Feign vers document-service
        return vehiculeDocumentService.uploadDocument(vehiculeId, file, typeDocument, numero, expiration);
    }

    @Override
    public List<DocumentResponse> getVehiculeDocuments(Long vehiculeId) {
        if (!vehiculeRepository.existsById(vehiculeId)) {
            throw new ResourceNotFoundException("Vehicule not found with id: " + vehiculeId);
        }
        return vehiculeDocumentService.getDocumentsByVehicule(vehiculeId);
    }
}
