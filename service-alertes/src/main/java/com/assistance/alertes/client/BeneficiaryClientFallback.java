package com.assistance.alertes.client;

import com.assistance.alertes.dto.feign.BeneficiaryDTO;
import com.assistance.alertes.dto.feign.ContactDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BeneficiaryClientFallback implements BeneficiaryClient {

    private static final Logger log = LoggerFactory.getLogger(BeneficiaryClientFallback.class);

    @Override
    public BeneficiaryDTO getBeneficiaryById(Long id) {
        log.warn("[Simulation Fallback] service-beneficiaires non joignable. Génération du profil simulé pour le bénéficiaire id={}", id);
        return BeneficiaryDTO.builder()
                .id(id)
                .nom("M. Robert Martin")
                .age(84)
                .adresse("14 Rue des Lilas, 75020 Paris")
                .pathologie("Insuffisance cardiaque modérée, troubles de l'équilibre")
                .telephone("01 43 56 78 90")
                .build();
    }

    @Override
    public List<ContactDTO> getContactsByBeneficiaryId(Long id) {
        log.warn("[Simulation Fallback] service-beneficiaires non joignable. Génération des contacts simulés pour le bénéficiaire id={}", id);
        ContactDTO c1 = ContactDTO.builder()
                .id(1L)
                .beneficiaryId(id)
                .nom("Claire Martin")
                .lien("Fille (Contact prioritaire)")
                .telephone("06 12 34 56 78")
                .build();

        ContactDTO c2 = ContactDTO.builder()
                .id(2L)
                .beneficiaryId(id)
                .nom("Dr. Alain Bernard")
                .lien("Médecin traitant")
                .telephone("01 45 67 89 10")
                .build();

        return List.of(c1, c2);
    }
}
