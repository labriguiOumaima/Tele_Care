package com.telecare.serviceinterventions.mapper;

import com.telecare.serviceinterventions.dto.InterventionCreationDto;
import com.telecare.serviceinterventions.dto.InterventionDto;
import com.telecare.serviceinterventions.entite.Intervention;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface InterventionMapper {

    InterventionDto toDto(Intervention intervention);

    // id, heureArrivee et compteRendu restent vides, et statut reste AFFECTEE (valeur par defaut de l'entite)
    Intervention toEntity(InterventionCreationDto dto);
}