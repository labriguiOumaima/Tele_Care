package com.telecare.serviceinterventions.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AssistantAstreinteService {

    // Liste provisoire d'identifiants d'assistants, en attendant service-utilisateurs
    private static final List<Long> ASSISTANTS = List.of(1L, 2L, 3L, 4L);

    private final AtomicInteger compteur = new AtomicInteger(0);

    // Choix a tour de role : chaque nouvelle intervention prend l'assistant suivant
    public Long prochainAssistant() {
        int index = Math.floorMod(compteur.getAndIncrement(), ASSISTANTS.size());
        return ASSISTANTS.get(index);
    }
}