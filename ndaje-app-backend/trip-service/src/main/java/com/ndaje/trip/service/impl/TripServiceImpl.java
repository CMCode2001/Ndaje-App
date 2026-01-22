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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;

    @Override
    public TripResponse createTrip(CreateTripRequest request) {
        Trajet trajet = Trajet.builder()
                .conducteurId(request.getConducteurId())
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

    private TripResponse mapToTripResponse(Trajet trajet) {
        return TripResponse.builder()
                .id(trajet.getId())
                .conducteurId(trajet.getConducteurId())
                .depart(trajet.getDepart())
                .arrivee(trajet.getArrivee())
                .dateDepart(trajet.getDateDepart())
                .placesDisponibles(trajet.getPlacesDisponibles())
                .prix(trajet.getPrix())
                .statutTrajet(trajet.getStatutTrajet())
                .build();
    }
}
