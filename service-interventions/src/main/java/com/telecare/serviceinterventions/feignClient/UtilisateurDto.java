package com.telecare.serviceinterventions.feignClient;

public record UtilisateurDto(
        Long id,
        String nom,
        String email,
        String role,
        Boolean actif
) {
}