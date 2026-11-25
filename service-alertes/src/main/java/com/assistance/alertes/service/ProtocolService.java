package com.assistance.alertes.service;

import com.assistance.alertes.dto.request.ProtocolRequestDTO;
import com.assistance.alertes.dto.response.ProtocolResponseDTO;
import com.assistance.alertes.entity.Protocol;
import com.assistance.alertes.exception.BadRequestException;
import com.assistance.alertes.exception.ResourceNotFoundException;
import com.assistance.alertes.repository.ProtocolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProtocolService {

    private final ProtocolRepository protocolRepository;

    public ProtocolService(ProtocolRepository protocolRepository) {
        this.protocolRepository = protocolRepository;
    }

    public ProtocolResponseDTO createProtocol(ProtocolRequestDTO request) {
        String normalizedType = request.getType().trim().toUpperCase();
        if (protocolRepository.existsByTypeIgnoreCase(normalizedType)) {
            throw new BadRequestException("Un protocole pour le type '" + normalizedType + "' existe déjà");
        }

        Protocol protocol = Protocol.builder()
                .type(normalizedType)
                .etapes(request.getEtapes())
                .niveauPriorite(request.getNiveauPriorite())
                .build();

        Protocol saved = protocolRepository.save(protocol);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProtocolResponseDTO> getAllProtocols() {
        return protocolRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProtocolResponseDTO getProtocolById(Long id) {
        Protocol protocol = protocolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Protocole non trouvé avec l'id: " + id));
        return mapToResponse(protocol);
    }

    @Transactional(readOnly = true)
    public ProtocolResponseDTO getProtocolByType(String type) {
        Protocol protocol = protocolRepository.findByTypeIgnoreCase(type.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun protocole configuré pour le type d'alerte: " + type));
        return mapToResponse(protocol);
    }

    public ProtocolResponseDTO updateProtocol(Long id, ProtocolRequestDTO request) {
        Protocol protocol = protocolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Protocole non trouvé avec l'id: " + id));

        String normalizedType = request.getType().trim().toUpperCase();
        if (!protocol.getType().equalsIgnoreCase(normalizedType) &&
                protocolRepository.existsByTypeIgnoreCase(normalizedType)) {
            throw new BadRequestException("Un autre protocole pour le type '" + normalizedType + "' existe déjà");
        }

        protocol.setType(normalizedType);
        protocol.setEtapes(request.getEtapes());
        protocol.setNiveauPriorite(request.getNiveauPriorite());

        Protocol updated = protocolRepository.save(protocol);
        return mapToResponse(updated);
    }

    public void deleteProtocol(Long id) {
        if (!protocolRepository.existsById(id)) {
            throw new ResourceNotFoundException("Protocole non trouvé avec l'id: " + id);
        }
        protocolRepository.deleteById(id);
    }

    public ProtocolResponseDTO mapToResponse(Protocol protocol) {
        return ProtocolResponseDTO.builder()
                .id(protocol.getId())
                .type(protocol.getType())
                .etapes(protocol.getEtapes())
                .niveauPriorite(protocol.getNiveauPriorite())
                .build();
    }
}
