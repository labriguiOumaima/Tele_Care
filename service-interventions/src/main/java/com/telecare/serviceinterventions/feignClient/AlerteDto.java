package com.telecare.serviceinterventions.feignClient;

import java.time.LocalDateTime;

public record AlerteDto(
        Long id,
        Long beneficiaryId,
        String type,
        String gravite,
        LocalDateTime dateDeclenchement,
        String statut
) {
}