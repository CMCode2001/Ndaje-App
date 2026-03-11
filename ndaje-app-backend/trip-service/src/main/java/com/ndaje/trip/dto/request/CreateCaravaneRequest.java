package com.ndaje.trip.dto.request;

import com.ndaje.trip.entity.ThemeCaravane;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaravaneRequest {

    @NotBlank(message = "L'ID du caravannier est obligatoire")
    private String caravannierId;

    @NotBlank(message = "Le nom de la caravane est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit faire entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 -]+$", message = "Le nom contient des caractères invalides")
    private String nom;

    @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
    private String description;

    @NotBlank(message = "Le lieu de départ est obligatoire")
    @Size(min = 2, max = 100, message = "Le lieu de départ doit faire entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 -]+$", message = "Le départ contient des caractères invalides")
    private String depart;

    @NotBlank(message = "Le lieu d'arrivée est obligatoire")
    @Size(min = 2, max = 100, message = "Le lieu d'arrivée doit faire entre 2 et 100 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 -]+$", message = "L'arrivée contient des caractères invalides")
    private String arrivee;

    @Size(max = 200, message = "Les étapes ne doivent pas dépasser 200 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ0-9 ,-]+$", message = "Les étapes contiennent des caractères invalides")
    private String etapes; // Optionnel : ex "Thiès, Diourbel"

    @NotNull(message = "La date de départ est obligatoire")
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDateTime dateDepart;

    @Future(message = "La date d'arrivée estimée doit être dans le futur")
    private LocalDateTime dateArriveeEstimee;

    @Min(value = 1, message = "Il doit y avoir au moins 1 participant")
    private int maxParticipants;

    @Min(value = 0, message = "Le prix ne peut pas être négatif")
    private double prixParPersonne;

    private String vehiculeIds; // Optionnel à la création

    private ThemeCaravane theme;
}
