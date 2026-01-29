package com.ndajee.userservice.entities;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
public class Passager extends Utilisateur {
    private int pointsFidelite;
    private Double noteMoyenne;
}
