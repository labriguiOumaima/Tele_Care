package com.telecare.utilisateurs.mapper;

import com.telecare.utilisateurs.dto.UtilisateurRequestDto;
import com.telecare.utilisateurs.dto.UtilisateurResponseDto;
import com.telecare.utilisateurs.entity.Utilisateur;
import org.springframework.stereotype.Component;

/**
 * Mapper manuel entre Entités JPA et DTOs (Jalon V1).
 */
@Component
public class UtilisateurMapper {

    public Utilisateur toEntity(UtilisateurRequestDto dto) {
        if (dto == null) {
            return null;
        }
        return Utilisateur.builder()
                .nom(dto.getNom())
                .email(dto.getEmail())
                .role(dto.getRole())
                .actif(true)
                .build();
    }

    public UtilisateurResponseDto toDto(Utilisateur entity) {
        if (entity == null) {
            return null;
        }
        return UtilisateurResponseDto.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .email(entity.getEmail())
                .role(entity.getRole())
                .actif(entity.isActif())
                .build();
    }
}
