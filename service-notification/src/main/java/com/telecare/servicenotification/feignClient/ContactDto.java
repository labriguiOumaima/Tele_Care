package com.telecare.servicenotification.feignClient;

public record ContactDto(
        Long id,
        Long beneficiaryId,
        String nom,
        String lien,
        String telephone
) {
}