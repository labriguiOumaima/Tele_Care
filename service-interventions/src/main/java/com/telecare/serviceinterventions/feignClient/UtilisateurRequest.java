package com.telecare.serviceinterventions.feignClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-utilisateurs")
public interface UtilisateurRequest {

    // Chemin supposé : à confirmer avec la Personne 4
    @GetMapping("/api/utilisateurs/{id}")
    UtilisateurDto getUtilisateurById(@PathVariable("id") Long id);
    @GetMapping("/api/utilisateurs/assistants-actifs")
    java.util.List<UtilisateurDto> getAssistantsActifs();
}
