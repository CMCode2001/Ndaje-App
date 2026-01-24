package com.ndajee.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "car-service")
public interface CarClient {

    @GetMapping("/api/cars/driver/{driverId}")
    List<Object> getCarsByDriverId(@PathVariable("driverId") String driverId);

    // Note: Object is used as placeholder until CarDTO is shared or duplicated
}
