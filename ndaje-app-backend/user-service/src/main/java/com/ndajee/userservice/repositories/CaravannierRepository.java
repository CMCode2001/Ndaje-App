package com.ndajee.userservice.repositories;

import com.ndajee.userservice.entities.Caravannier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaravannierRepository extends JpaRepository<Caravannier, String> {
}
