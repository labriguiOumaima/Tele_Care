package com.telecare.utilisateurs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO pour les requêtes entrantes de création et modification d'utilisateur.
 * Respecte l'isolation de la couche domaine (Jalon V1).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurRequestDto {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email est invalide")
    private String email;

    @NotBlank(message = "Le role est obligatoire")
    private String role;
}
