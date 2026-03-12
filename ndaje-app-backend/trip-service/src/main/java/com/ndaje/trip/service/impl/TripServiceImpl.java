package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.TripNotFoundException;
import com.ndaje.trip.repository.TripRepository;
import com.ndaje.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ndaje.trip.security.SecurityUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final com.ndaje.trip.client.UserClient userClient;
    private final com.ndaje.trip.client.CarClient carClient;

    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        String driverId = request.getDriverId();

        // 1. Extract driverId from SecurityContext if not provided
        if (driverId == null || driverId.isBlank()) {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (authentication instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtToken) {
                driverId = jwtToken.getName(); // Usually the 'sub' claim (Keycloak userId)
            }
        }

        if (driverId == null || driverId.isBlank()) {
            throw new com.ndaje.trip.exception.BusinessException(
                    "Driver ID is required and could not be determined from security context");
        }

        // 2. Resolve Vehicle ID if not provided
        String vehicleId = request.getVehicleId();
        if (vehicleId == null || vehicleId.isBlank()) {
            List<com.ndaje.trip.dto.response.VehiculeDto> vehicles = carClient.getVehiculesByDriverId(driverId);
            if (vehicles.isEmpty()) {
                throw new com.ndaje.trip.exception.BusinessException(
                        "No vehicle found for this driver. Please register a vehicle first.");
            } else if (vehicles.size() == 1) {
                vehicleId = String.valueOf(vehicles.get(0).getId());
            } else {
                throw new com.ndaje.trip.exception.BusinessException(
                        "Multiple vehicles found. Please specify which vehicle to use.");
            }
        }

        // Validate Driver (Optional if we trust Keycloak, but good for sync)
        try {
            userClient.getUserById(driverId);
        } catch (Exception e) {
            throw new com.ndaje.trip.exception.BusinessException("Invalid Driver ID or User Service unavailable");
        }

        // Validate Vehicle
        try {
            carClient.getVehiculeById(Long.parseLong(vehicleId));
        } catch (Exception e) {
            throw new com.ndaje.trip.exception.BusinessException("Invalid Vehicle ID or Vehicle Service unavailable");
        }

        Trajet trajet = Trajet.builder()
                .driverId(driverId)
                .vehicleId(vehicleId)
                .depart(request.getDepart())
                .arrivee(request.getArrivee())
                .dateDepart(request.getDateDepart())
                .placesDisponibles(request.getPlacesDisponibles())
                .prix(request.getPrix())
                .statutTrajet(StatutTrajet.CREATED)
                .build();

        Trajet savedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(savedTrajet);
    }

    @Override
    @Transactional(readOnly = true)
    public TripResponse getTripById(Long id) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));
        return mapToTripResponse(trajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getAllTrips() {
        return tripRepository.findAll().stream()
                .map(this::mapToTripResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse updateTripStatus(Long id, StatutTrajet status) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        // Enforce BOLA/IDOR protection
        SecurityUtils.verifyOwnership(trajet.getDriverId());

        trajet.setStatutTrajet(status);
        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    @Override
    public TripResponse decrementSeats(Long id, int quantity) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        // Enforce BOLA/IDOR protection
        SecurityUtils.verifyOwnership(trajet.getDriverId());

        if (trajet.getPlacesDisponibles() < quantity) {
            throw new com.ndaje.trip.exception.BusinessException("Not enough seats available");
        }

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - quantity);
        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByDriverId(String driverId) {
        return tripRepository.findByDriverId(driverId).stream()
                .map(this::mapToTripResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse updateTrip(Long id, com.ndaje.trip.dto.request.UpdateTripRequest request) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        // Enforce BOLA/IDOR protection
        SecurityUtils.verifyOwnership(trajet.getDriverId());

        // Update fields if present (assuming non-null means update, or we can just
        // overwrite)
        // Since UpdateTripRequest has validation constraints, we can assume valid if
        // passed @Valid in controller
        // But for update usually we allow partial. The DTO I made has constraints.
        // If the controller uses @Valid, then all fields must be valid.

        if (request.getDepart() != null)
            trajet.setDepart(request.getDepart());
        if (request.getArrivee() != null)
            trajet.setArrivee(request.getArrivee());
        if (request.getDateDepart() != null)
            trajet.setDateDepart(request.getDateDepart());
        if (request.getPlacesDisponibles() != null)
            trajet.setPlacesDisponibles(request.getPlacesDisponibles());
        if (request.getPrix() != null)
            trajet.setPrix(request.getPrix());

        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    private TripResponse mapToTripResponse(Trajet trajet) {
        return TripResponse.builder()
                .id(trajet.getId())
                .driverId(trajet.getDriverId())
                .vehicleId(trajet.getVehicleId())
                .depart(trajet.getDepart())
                .arrivee(trajet.getArrivee())
                .dateDepart(trajet.getDateDepart())
                .placesDisponibles(trajet.getPlacesDisponibles())
                .prix(trajet.getPrix())
                .statutTrajet(trajet.getStatutTrajet())
                .build();
    }
}
