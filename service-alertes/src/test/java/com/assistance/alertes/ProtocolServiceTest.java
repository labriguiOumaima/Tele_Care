package com.assistance.alertes;

import com.assistance.alertes.dto.request.ProtocolRequestDTO;
import com.assistance.alertes.dto.response.ProtocolResponseDTO;
import com.assistance.alertes.exception.BadRequestException;
import com.assistance.alertes.exception.ResourceNotFoundException;
import com.assistance.alertes.service.ProtocolService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest(properties={"spring.cloud.config.enabled=false","spring.config.import="})
@Transactional
class ProtocolServiceTest {

    @Autowired
    private ProtocolService protocolService;

    @Test
    void testGetAllProtocols() {
        List<ProtocolResponseDTO> protocols = protocolService.getAllProtocols();
        assertNotNull(protocols);
        assertFalse(protocols.isEmpty());
    }

    @Test
    void testGetProtocolByType() {
        ProtocolResponseDTO protocol = protocolService.getProtocolByType("CHUTE");
        assertNotNull(protocol);
        assertEquals("CHUTE", protocol.getType());
        assertEquals(4, protocol.getNiveauPriorite());
    }

    @Test
    void testGetProtocolByTypeNotFound() {
        assertThrows(ResourceNotFoundException.class, () -> {
            protocolService.getProtocolByType("TYPE_INEXISTANT");
        });
    }

    @Test
    void testCreateProtocolSuccess() {
        ProtocolRequestDTO request = ProtocolRequestDTO.builder()
                .type("INONDATION")
                .etapes("1. Couper l'électricité. 2. Contacter les secours.")
                .niveauPriorite(3)
                .build();

        ProtocolResponseDTO created = protocolService.createProtocol(request);
        assertNotNull(created.getId());
        assertEquals("INONDATION", created.getType());
    }

    @Test
    void testCreateDuplicateProtocolThrowsException() {
        ProtocolRequestDTO request = ProtocolRequestDTO.builder()
                .type("CHUTE")
                .etapes("Doublon")
                .niveauPriorite(4)
                .build();

        assertThrows(BadRequestException.class, () -> {
            protocolService.createProtocol(request);
        });
    }

    @Test
    void testDeleteProtocol() {
        ProtocolRequestDTO request = ProtocolRequestDTO.builder()
                .type("TEMP_PROTOCOLE")
                .etapes("Etapes temporaires")
                .niveauPriorite(1)
                .build();

        ProtocolResponseDTO created = protocolService.createProtocol(request);
        assertNotNull(created.getId());

        protocolService.deleteProtocol(created.getId());

        assertThrows(ResourceNotFoundException.class, () -> {
            protocolService.getProtocolById(created.getId());
        });
    }
}
