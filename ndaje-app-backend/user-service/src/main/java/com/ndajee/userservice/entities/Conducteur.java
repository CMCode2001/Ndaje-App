package com.ndajee.userservice.entities;

import com.ndajee.userservice.enums.StatutConducteur;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Conducteur extends Utilisateur {

    @Enumerated(EnumType.STRING)
    private StatutConducteur statut;
}
