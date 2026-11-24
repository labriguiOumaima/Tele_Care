package com.telecare.utilisateurs.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * DTO de réponse paginée pour la liste des utilisateurs.
 * Encapsule les UtilisateurResponseDto (Jalon V1).
 */
public record UtilisateurPageDto(
        List<UtilisateurResponseDto> contenu,
        int page,
        int taille,
        long totalElements,
        int totalPages,
        boolean dernierePage
) {
    public static UtilisateurPageDto from(Page<UtilisateurResponseDto> page) {
        return new UtilisateurPageDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
