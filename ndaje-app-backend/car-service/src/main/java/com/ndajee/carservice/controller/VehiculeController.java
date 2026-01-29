package com.ndajee.carservice.controller;

import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import org.springframework.http.MediaType;
import com.ndajee.carservice.dto.DocumentResponse;
import com.ndajee.carservice.service.VehiculeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @PostMapping
    public ResponseEntity<VehiculeResponse> createVehicule(@RequestBody VehiculeRequest vehiculeRequest) {
        return new ResponseEntity<>(vehiculeService.createVehicule(vehiculeRequest), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculeResponse> updateVehicule(@PathVariable Long id,
            @RequestBody VehiculeRequest vehiculeRequest) {
        return ResponseEntity.ok(vehiculeService.updateVehicule(id, vehiculeRequest));
    }

    @GetMapping
    public ResponseEntity<List<VehiculeResponse>> getAllVehicules() {
        return ResponseEntity.ok(vehiculeService.getAllVehicules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculeResponse> getVehiculeById(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeById(id)
                .orElseThrow(() -> new com.ndajee.carservice.exception.ResourceNotFoundException(
                        "Vehicule not found with id: " + id)));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<VehiculeResponse>> getVehiculesByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(vehiculeService.getVehiculesByDriverId(driverId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicule(@PathVariable Long id) {
        vehiculeService.deleteVehicule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeDocument") String typeDocument,
            @RequestParam("numero") String numero,
            @RequestParam(value = "expiration", required = false) String expiration) {
        return new ResponseEntity<>(vehiculeService.uploadDocument(id, file, typeDocument, numero, expiration),
                HttpStatus.CREATED);
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<List<DocumentResponse>> getVehiculeDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculeService.getVehiculeDocuments(id));
    }
}
