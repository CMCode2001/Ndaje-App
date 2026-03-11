package com.ndajee.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit faire entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ -]+$", message = "Le prénom contient des caractères invalides")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit faire entre 2 et 50 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ -]+$", message = "Le nom contient des caractères invalides")
    private String nom;

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "Le format du numéro de téléphone est invalide")
    private String telephone;
}
