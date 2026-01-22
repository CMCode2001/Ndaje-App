package com.ndaje.trip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur simple pour le service de gestion des trajets.
 */
@RestController
@RequestMapping("/api/trips")
public class TripController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Trip Service!";
    }
}
