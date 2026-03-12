package com.ndajee.userservice.entities;

import com.ndajee.userservice.entities.base.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public abstract class Utilisateur extends Auditable {

    @Id
    @Column(nullable = false, unique = true)
    private String id;

    private String prenom;
    private String nom;
    @Convert(converter = com.ndajee.userservice.config.EncryptionConverter.class)
    private String email;

    @Convert(converter = com.ndajee.userservice.config.EncryptionConverter.class)
    private String telephone;

    private String role;

    private boolean actif = true;

}
