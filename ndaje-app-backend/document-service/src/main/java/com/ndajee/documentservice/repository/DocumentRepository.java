package com.ndajee.documentservice.repository;

import com.ndajee.documentservice.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour la gestion de la persistance des documents.
 */
@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    /**
     * Récupère la liste des documents appartenant à un utilisateur spécifique.
     * @param utilisateurId ID de l'utilisateur
     * @return Liste de documents
     */
    List<Document> findByUtilisateurId(String utilisateurId);
}
