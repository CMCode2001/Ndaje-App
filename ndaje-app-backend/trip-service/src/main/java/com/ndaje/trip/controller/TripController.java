package com.ndaje.trip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Trip Service!";
    }
}
