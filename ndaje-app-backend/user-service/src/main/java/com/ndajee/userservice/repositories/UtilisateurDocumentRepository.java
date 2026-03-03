package com.ndajee.userservice.repositories;

import com.ndajee.userservice.entities.UtilisateurDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UtilisateurDocumentRepository extends JpaRepository<UtilisateurDocument, Long> {

    List<UtilisateurDocument> findByUserId(String userId);

    void deleteByUserId(String userId);
}
