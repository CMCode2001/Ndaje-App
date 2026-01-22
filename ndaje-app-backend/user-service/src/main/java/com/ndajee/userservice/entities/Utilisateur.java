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
    private String email;
    private String telephone;
    
    private boolean actif=true;

}
