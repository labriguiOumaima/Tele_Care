package com.telecare.utilisateurs.dto;

import lombok.Data;

@Data
public class VerificationUtilisateurDTO {

    private Long utilisateurId;
    private boolean existe;
    private boolean actif;
    private boolean roleValide;
    private String role;
    private String message;
}
