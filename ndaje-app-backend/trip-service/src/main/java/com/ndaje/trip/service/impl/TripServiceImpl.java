package com.ndaje.trip.service.impl;

import com.ndaje.trip.dto.request.CreateTripRequest;
import com.ndaje.trip.dto.response.TripResponse;
import com.ndaje.trip.dto.response.UserDto;
import com.ndaje.trip.dto.response.VehiculeDto;
import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.exception.TripNotFoundException;
import com.ndaje.trip.repository.TripRepository;
import com.ndaje.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                driverId = jwtToken.getName();
            }
        }

        if (driverId == null || driverId.isBlank()) {
            throw new com.ndaje.trip.exception.BusinessException(
                    "Driver ID is required and could not be determined from security context");
        }

        // 2. Resolve Vehicle ID if not provided
        String vehicleId = request.getVehicleId();
        if (vehicleId == null || vehicleId.isBlank()) {
            List<VehiculeDto> vehicles = carClient.getVehiculesByDriverId(driverId);
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

        // Validate Driver
        UserDto driver = null;
        try {
            driver = userClient.getUserById(driverId);
        } catch (Exception e) {
            throw new com.ndaje.trip.exception.BusinessException("Invalid Driver ID or User Service unavailable");
        }

        // Validate Vehicle
        VehiculeDto vehicle;
        try {
            vehicle = carClient.getVehiculeById(Long.parseLong(vehicleId));
        } catch (Exception e) {
            throw new com.ndaje.trip.exception.BusinessException("Invalid Vehicle ID or Vehicle Service unavailable");
        }

        // Validate available seats against vehicle capacity
        if (request.getPlacesDisponibles() > vehicle.getPlaces()) {
            throw new com.ndaje.trip.exception.BusinessException(
                    "Available seats (" + request.getPlacesDisponibles() + ") cannot exceed vehicle capacity ("
                            + vehicle.getPlaces() + ")");
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
        return mapToTripResponse(savedTrajet, driver, vehicle);
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
        List<Trajet> trajets = tripRepository.findAll();
        java.util.Map<String, UserDto> driverCache = new java.util.HashMap<>();
        java.util.Map<Long, VehiculeDto> vehicleCache = new java.util.HashMap<>();

        return trajets.stream()
                .map(trajet -> {
                    UserDto driver = driverCache.computeIfAbsent(trajet.getDriverId(), id -> {
                        try {
                            return userClient.getUserById(id);
                        } catch (Exception e) {
                            return null;
                        }
                    });
                    VehiculeDto vehicle = vehicleCache.computeIfAbsent(Long.parseLong(trajet.getVehicleId()), id -> {
                        try {
                            return carClient.getVehiculeById(id);
                        } catch (Exception e) {
                            return null;
                        }
                    });
                    return mapToTripResponse(trajet, driver, vehicle);
                })
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse updateTripStatus(Long id, StatutTrajet status) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        trajet.setStatutTrajet(status);
        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    @Override
    public TripResponse decrementSeats(Long id, int quantity) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        if (trajet.getPlacesDisponibles() < quantity) {
            throw new com.ndaje.trip.exception.BusinessException("Not enough seats available");
        }

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() - quantity);
        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    @Override
    public TripResponse incrementSeats(Long id, int quantity) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        trajet.setPlacesDisponibles(trajet.getPlacesDisponibles() + quantity);
        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripResponse> getTripsByDriverId(String driverId) {
        UserDto driver = null;
        try {
            driver = userClient.getUserById(driverId);
        } catch (Exception e) {
        }

        final UserDto finalDriver = driver;
        List<Trajet> trajets = tripRepository.findByDriverId(driverId);
        java.util.Map<Long, VehiculeDto> vehicleCache = new java.util.HashMap<>();

        return trajets.stream()
                .map(trajet -> {
                    VehiculeDto vehicle = vehicleCache.computeIfAbsent(Long.parseLong(trajet.getVehicleId()), id -> {
                        try {
                            return carClient.getVehiculeById(id);
                        } catch (Exception e) {
                            return null;
                        }
                    });
                    return mapToTripResponse(trajet, finalDriver, vehicle);
                })
                .collect(Collectors.toList());
    }

    @Override
    public TripResponse updateTrip(Long id, com.ndaje.trip.dto.request.UpdateTripRequest request) {
        Trajet trajet = tripRepository.findById(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with id: " + id));

        if (request.getDepart() != null)
            trajet.setDepart(request.getDepart());
        if (request.getArrivee() != null)
            trajet.setArrivee(request.getArrivee());
        if (request.getDateDepart() != null)
            trajet.setDateDepart(request.getDateDepart());
        if (request.getPlacesDisponibles() != null) {
            VehiculeDto vehicle;
            try {
                vehicle = carClient.getVehiculeById(Long.parseLong(trajet.getVehicleId()));
                if (request.getPlacesDisponibles() > vehicle.getPlaces()) {
                    throw new com.ndaje.trip.exception.BusinessException(
                            "Available seats (" + request.getPlacesDisponibles() + ") cannot exceed vehicle capacity ("
                                    + vehicle.getPlaces() + ")");
                }
            } catch (com.ndaje.trip.exception.BusinessException be) {
                throw be;
            } catch (Exception e) {
            }
            trajet.setPlacesDisponibles(request.getPlacesDisponibles());
        }
        if (request.getPrix() != null)
            trajet.setPrix(request.getPrix());

        Trajet updatedTrajet = tripRepository.save(trajet);
        return mapToTripResponse(updatedTrajet);
    }

    private TripResponse mapToTripResponse(Trajet trajet) {
        UserDto driver = null;
        try {
            driver = userClient.getUserById(trajet.getDriverId());
        } catch (Exception e) {
        }
        
        VehiculeDto vehicle = null;
        try {
            vehicle = carClient.getVehiculeById(Long.parseLong(trajet.getVehicleId()));
        } catch (Exception e) {
        }
        
        return mapToTripResponse(trajet, driver, vehicle);
    }

    private TripResponse mapToTripResponse(Trajet trajet, UserDto driver, VehiculeDto vehicle) {
        return TripResponse.builder()
                .id(trajet.getId())
                .driverId(trajet.getDriverId())
                .driverFirstName(driver != null ? driver.getPrenom() : null)
                .driverLastName(driver != null ? driver.getNom() : null)
                .driverPhone(driver != null ? driver.getTelephone() : null)
                .vehicleId(trajet.getVehicleId())
                .vehicleMarque(vehicle != null ? vehicle.getMarque() : null)
                .vehicleModele(vehicle != null ? vehicle.getModele() : null)
                .vehicleImmatriculation(vehicle != null ? vehicle.getImmatriculation() : null)
                .depart(trajet.getDepart())
                .arrivee(trajet.getArrivee())
                .dateDepart(trajet.getDateDepart())
                .placesDisponibles(trajet.getPlacesDisponibles())
                .prix(trajet.getPrix())
                .statutTrajet(trajet.getStatutTrajet())
                .build();
    }
}
