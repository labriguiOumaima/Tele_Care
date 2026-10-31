package com.telecare.serviceinterventions.dto;

import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;

public record ArriveeDto(
        @PastOrPresent(message = "heureArrivee ne peut pas etre dans le futur")
        LocalDateTime heureArrivee
) {
}