package com.telecare.beneficiaires.servicebeneficiaires.dto;
import java.util.List;

public record BeneficiaryDossierResponse(
        BeneficiaryResponse beneficiaire,
        List<ContactResponse> contacts
) {
}