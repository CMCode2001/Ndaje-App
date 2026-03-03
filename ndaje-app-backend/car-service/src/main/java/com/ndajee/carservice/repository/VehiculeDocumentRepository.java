package com.ndajee.carservice.repository;

import com.ndajee.carservice.domain.VehiculeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehiculeDocumentRepository extends JpaRepository<VehiculeDocument, Long> {

    List<VehiculeDocument> findByVehiculeId(Long vehiculeId);

    void deleteByVehiculeId(Long vehiculeId);
}
