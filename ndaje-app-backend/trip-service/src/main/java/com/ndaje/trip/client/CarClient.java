package com.ndaje.trip.client;

import com.ndaje.trip.dto.response.VehiculeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "car-service", url = "${application.config.car-service-url}")
public interface CarClient {

    @GetMapping("/api/vehicules/{id}")
    VehiculeDto getVehiculeById(@PathVariable("id") Long id);

    @GetMapping("/api/vehicules/driver/{driverId}")
    java.util.List<VehiculeDto> getVehiculesByDriverId(@PathVariable("driverId") String driverId);
}
