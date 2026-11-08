package com.telecare.serviceinterventions.feignClient;

public record ProtocolDto(
        Long id,
        String type,
        String etapes,
        Integer niveauPriorite
) {
}