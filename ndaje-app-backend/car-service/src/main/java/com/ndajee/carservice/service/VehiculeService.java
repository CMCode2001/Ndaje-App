package com.ndajee.carservice.service;

import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import com.ndajee.carservice.dto.DocumentResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface VehiculeService {
    VehiculeResponse createVehicule(VehiculeRequest vehiculeRequest);

    VehiculeResponse updateVehicule(Long id, VehiculeRequest vehiculeRequest);

    List<VehiculeResponse> getAllVehicules();

    Optional<VehiculeResponse> getVehiculeById(Long id);

    List<VehiculeResponse> getVehiculesByDriverId(String driverId);

    void deleteVehicule(Long id);

    DocumentResponse uploadDocument(Long vehiculeId, MultipartFile file, String typeDocument, String numero,
            String expiration);

    List<DocumentResponse> getVehiculeDocuments(Long vehiculeId);
}
