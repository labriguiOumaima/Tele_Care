package com.assistance.alertes.controller;

import com.assistance.alertes.dto.request.ProtocolRequestDTO;
import com.assistance.alertes.dto.response.ProtocolResponseDTO;
import com.assistance.alertes.service.ProtocolService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/protocols")
@CrossOrigin(origins = "*")
public class ProtocolController {

    private final ProtocolService protocolService;

    public ProtocolController(ProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    /**
     * Créer un nouveau protocole de réponse
     */
    @PostMapping
    public ResponseEntity<ProtocolResponseDTO> createProtocol(@Valid @RequestBody ProtocolRequestDTO request) {
        ProtocolResponseDTO created = protocolService.createProtocol(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Consulter tous les protocoles
     */
    @GetMapping
    public ResponseEntity<List<ProtocolResponseDTO>> getAllProtocols() {
        List<ProtocolResponseDTO> protocols = protocolService.getAllProtocols();
        return ResponseEntity.ok(protocols);
    }

    /**
     * Consulter un protocole par son ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProtocolResponseDTO> getProtocolById(@PathVariable Long id) {
        ProtocolResponseDTO protocol = protocolService.getProtocolById(id);
        return ResponseEntity.ok(protocol);
    }

    /**
     * Récupérer un protocole selon le type d'alerte (ex: CHUTE, MALAISE)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<ProtocolResponseDTO> getProtocolByType(@PathVariable String type) {
        ProtocolResponseDTO protocol = protocolService.getProtocolByType(type);
        return ResponseEntity.ok(protocol);
    }

    /**
     * Mettre à jour un protocole
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProtocolResponseDTO> updateProtocol(
            @PathVariable Long id,
            @Valid @RequestBody ProtocolRequestDTO request) {
        ProtocolResponseDTO updated = protocolService.updateProtocol(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprimer un protocole
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProtocol(@PathVariable Long id) {
        protocolService.deleteProtocol(id);
        return ResponseEntity.noContent().build();
    }
}
