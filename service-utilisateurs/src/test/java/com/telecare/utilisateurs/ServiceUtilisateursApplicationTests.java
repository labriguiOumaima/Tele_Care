package com.telecare.utilisateurs;

import com.telecare.utilisateurs.dto.UtilisateurRequestDto;
import com.telecare.utilisateurs.dto.UtilisateurResponseDto;
import com.telecare.utilisateurs.dto.VerificationUtilisateurDTO;
import com.telecare.utilisateurs.entity.Utilisateur;
import com.telecare.utilisateurs.exception.UtilisateurDejaExistantException;
import com.telecare.utilisateurs.mapper.UtilisateurMapper;
import com.telecare.utilisateurs.repository.UtilisateurRepository;
import com.telecare.utilisateurs.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceUtilisateursApplicationTests {

    @Mock
    private UtilisateurRepository repository;

    @Spy
    private UtilisateurMapper mapper = new UtilisateurMapper();

    @InjectMocks
    private UtilisateurService service;

    private UtilisateurRequestDto requestDto;
    private Utilisateur utilisateurTest;

    @BeforeEach
    void setUp() {
        requestDto = UtilisateurRequestDto.builder()
                .nom("Sara Benali")
                .email("sara.benali@telecare.ma")
                .role("ASSISTANT")
                .build();

        utilisateurTest = Utilisateur.builder()
                .id(1L)
                .nom("Sara Benali")
                .email("sara.benali@telecare.ma")
                .role("ASSISTANT")
                .actif(true)
                .build();
    }

    @Test
    void testAjouterUtilisateurSucces() {
        when(repository.existsByEmail(requestDto.getEmail())).thenReturn(false);
        when(repository.save(any(Utilisateur.class))).thenReturn(utilisateurTest);

        UtilisateurResponseDto cree = service.ajouter(requestDto);

        assertNotNull(cree);
        assertEquals("sara.benali@telecare.ma", cree.getEmail());
        assertTrue(cree.isActif());
        verify(repository, times(1)).save(any(Utilisateur.class));
    }

    @Test
    void testAjouterUtilisateurEmailExistant() {
        when(repository.existsByEmail(requestDto.getEmail())).thenReturn(true);

        assertThrows(UtilisateurDejaExistantException.class, () -> service.ajouter(requestDto));
        verify(repository, never()).save(any());
    }

    @Test
    void testVerifierUtilisateurValide() {
        when(repository.findById(1L)).thenReturn(Optional.of(utilisateurTest));

        VerificationUtilisateurDTO dto = service.verifierUtilisateur(1L, "ASSISTANT");

        assertTrue(dto.isExiste());
        assertTrue(dto.isActif());
        assertTrue(dto.isRoleValide());
        assertEquals("ASSISTANT", dto.getRole());
    }

    @Test
    void testChangerEtat() {
        when(repository.findById(1L)).thenReturn(Optional.of(utilisateurTest));
        when(repository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        UtilisateurResponseDto modifie = service.changerEtat(1L, false);

        assertFalse(modifie.isActif());
    }
}
