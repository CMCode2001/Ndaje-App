package com.ndaje.trip.repository;

import com.ndaje.trip.entity.Caravane;
import com.ndaje.trip.entity.StatutCaravane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CaravaneRepository extends JpaRepository<Caravane, Long> {

    // Trouver toutes les caravanes d'un caravannier
    List<Caravane> findByCaravannierId(String caravannierId);

    // Trouver les caravanes ouvertes avec de la place, dont la date de départ est
    // future
    List<Caravane> findByStatutAndPlacesDisponiblesGreaterThanAndDateDepartAfter(
            StatutCaravane statut, int placesDisponibles, LocalDateTime date);

    // Rechercher par ville de départ ou d'arrivée
    List<Caravane> findByDepartIgnoreCaseOrArriveeIgnoreCase(String depart, String arrivee);
}
