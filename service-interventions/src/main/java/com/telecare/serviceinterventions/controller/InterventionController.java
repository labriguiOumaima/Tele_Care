package com.telecare.serviceinterventions.controller;

import com.telecare.serviceinterventions.dto.ArriveeDto;
import com.telecare.serviceinterventions.dto.CompteRenduDto;
import com.telecare.serviceinterventions.dto.InterventionCreationDto;
import com.telecare.serviceinterventions.dto.InterventionDto;
import com.telecare.serviceinterventions.entite.Intervention;
import com.telecare.serviceinterventions.entite.StatutIntervention;
import com.telecare.serviceinterventions.mapper.InterventionMapper;
import com.telecare.serviceinterventions.service.InterventionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/interventions")
public class InterventionController {

    private final InterventionService interventionService;
    private final InterventionMapper interventionMapper;

    public InterventionController(InterventionService interventionService,
                                  InterventionMapper interventionMapper) {
        this.interventionService = interventionService;
        this.interventionMapper = interventionMapper;
    }

    // GET /interventions  ou  GET /interventions?statut=SUR_PLACE
    @GetMapping
    public ResponseEntity<List<InterventionDto>> getAllInterventions(
            @RequestParam(required = false) StatutIntervention statut) {
        return ResponseEntity.ok(interventionService.getAllInterventions(statut)
                .stream().map(interventionMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterventionDto> getInterventionById(@PathVariable Long id) {
        return ResponseEntity.ok(interventionMapper.toDto(interventionService.getInterventionById(id)));
    }

    @PostMapping("/new")
    public ResponseEntity<InterventionDto> creerIntervention(
            @Valid @RequestBody InterventionCreationDto dto) {
        Intervention intervention = interventionService.creerIntervention(interventionMapper.toEntity(dto));
        return ResponseEntity.created(URI.create("/interventions/" + intervention.getId()))
                .body(interventionMapper.toDto(intervention));
    }

    // Le corps est facultatif : sans heure, le service prend l'heure actuelle
    @PutMapping("/{id}/arrivee")
    public ResponseEntity<InterventionDto> enregistrerArrivee(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) ArriveeDto dto) {
        var heure = (dto == null) ? null : dto.heureArrivee();
        return ResponseEntity.ok(interventionMapper.toDto(interventionService.enregistrerArrivee(id, heure)));
    }

    @PutMapping("/{id}/compte-rendu")
    public ResponseEntity<InterventionDto> redigerCompteRendu(
            @PathVariable Long id,
            @Valid @RequestBody CompteRenduDto dto) {
        return ResponseEntity.ok(interventionMapper.toDto(
                interventionService.redigerCompteRendu(id, dto.compteRendu())));
    }

    @PutMapping("/{id}/cloture")
    public ResponseEntity<InterventionDto> cloturer(@PathVariable Long id) {
        return ResponseEntity.ok(interventionMapper.toDto(interventionService.cloturer(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimerIntervention(@PathVariable Long id) {
        interventionService.supprimerIntervention(id);
        return ResponseEntity.noContent().build();
    }
}