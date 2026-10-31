package com.telecare.beneficiaires.servicebeneficiaires.dto;

public record BeneficiaryResponse(
        Long id,
        String nom,
        Integer age,
        String adresse,
        String pathologie,
        String telephone
) {
}