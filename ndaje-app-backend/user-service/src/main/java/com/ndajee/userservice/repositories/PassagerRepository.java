package com.ndajee.userservice.repositories;

import com.ndajee.userservice.entities.Passager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PassagerRepository extends JpaRepository<Passager, String> {
}
