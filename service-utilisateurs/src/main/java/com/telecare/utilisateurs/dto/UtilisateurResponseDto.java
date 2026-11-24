package com.telecare.utilisateurs.dto;

import lombok.*;

/**
 * DTO de réponse pour l'exposition publique d'un utilisateur.
 * Protège les données internes du modèle JPA (Jalon V1).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurResponseDto {

    private Long id;
    private String nom;
    private String email;
    private String role;
    private boolean actif;
}
