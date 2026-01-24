package com.ndajee.carservice.mapper;

import com.ndajee.carservice.domain.Vehicule;
import com.ndajee.carservice.dto.VehiculeRequest;
import com.ndajee.carservice.dto.VehiculeResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VehiculeMapper {
    Vehicule toEntity(VehiculeRequest request);

    VehiculeResponse toResponse(Vehicule entity);

    List<VehiculeResponse> toResponseList(List<Vehicule> entities);

    void updateEntityFromRequest(VehiculeRequest request, @MappingTarget Vehicule entity);
}
