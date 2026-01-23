package com.ndajee.userservice.repositories;

import com.ndajee.userservice.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, String> {
}
