package com.ndajee.userservice.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Caravannier extends Utilisateur {

    private String nomEntreprise; // Nom de l'agence ou de l'association (optionnel)
    private String description; // Description de son activité (ex: Oustaz organisant des pèlerinages)

}
