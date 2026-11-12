package com.telecare.serviceinterventions.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AssistantAstreinteService {

    // Liste fictive d'assistants d'astreinte (suffisante pour cette version)
    private static final List<String> ASSISTANTS = List.of(
            "Karim Benali",
            "Nadia Alaoui",
            "Youssef Tazi",
            "Salma Idrissi");

    private final AtomicInteger compteur = new AtomicInteger(0);

    // Choix a tour de role : chaque nouvelle intervention prend l'assistant suivant
    public String prochainAssistant() {
        int index = Math.floorMod(compteur.getAndIncrement(), ASSISTANTS.size());
        return ASSISTANTS.get(index);
    }
}