package com.ndajee.userservice.repositories;

import com.ndajee.userservice.entities.Conducteur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConducteurRepository extends JpaRepository<Conducteur, String> {
}
