package com.ndajee.carservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @GetMapping("/api/documents/vehicle/{vehicleId}")
    List<Object> getDocumentsByVehicleId(@PathVariable("vehicleId") Long vehicleId);
}
