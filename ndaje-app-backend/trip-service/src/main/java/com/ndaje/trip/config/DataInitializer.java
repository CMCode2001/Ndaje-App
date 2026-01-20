package com.ndaje.trip.config;

import com.ndaje.trip.entity.StatutTrajet;
import com.ndaje.trip.entity.Trajet;
import com.ndaje.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final TripRepository tripRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (tripRepository.count() == 0) {
                Trajet trip1 = Trajet.builder()
                        .conducteurId(101L)
                        .depart("Guediawaye")
                        .arrivee("Plateau")
                        .dateDepart(LocalDateTime.now().plusDays(2))
                        .placesDisponibles(4)
                        .prix(1200)
                        .statutTrajet(StatutTrajet.CREATED)
                        .build();

                Trajet trip2 = Trajet.builder()
                        .conducteurId(102L)
                        .depart("Yoff")
                        .arrivee("Point-E")
                        .dateDepart(LocalDateTime.now().plusDays(5))
                        .placesDisponibles(3)
                        .prix(1000)
                        .statutTrajet(StatutTrajet.CREATED)
                        .build();

                tripRepository.saveAll(List.of(trip1, trip2));
                System.out.println("--- Test data initialized: 2 trips created ---");
            }
        };
    }
}
