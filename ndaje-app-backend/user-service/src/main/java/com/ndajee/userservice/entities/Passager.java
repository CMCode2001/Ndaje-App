package com.ndajee.userservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Entity
@Getter
@Setter
public class Passager extends Utilisateur {
    private int pointsFidelite;
}
