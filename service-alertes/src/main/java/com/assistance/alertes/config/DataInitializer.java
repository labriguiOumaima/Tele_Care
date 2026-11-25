package com.assistance.alertes.config;

import com.assistance.alertes.entity.Alert;
import com.assistance.alertes.entity.AlertStatus;
import com.assistance.alertes.entity.Protocol;
import com.assistance.alertes.repository.AlertRepository;
import com.assistance.alertes.repository.ProtocolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProtocolRepository protocolRepository;
    private final AlertRepository alertRepository;

    public DataInitializer(ProtocolRepository protocolRepository, AlertRepository alertRepository) {
        this.protocolRepository = protocolRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    public void run(String... args) {
        if (protocolRepository.count() == 0) {
            log.info("Initialisation des protocoles d'urgence de référence...");
            Protocol p1 = Protocol.builder()
                    .type("CHUTE")
                    .etapes("1. Établir le contact vocal via l'interphone. 2. Vérifier la réactivité du bénéficiaire. 3. Alerter l'assistant d'astreinte et le contact d'urgence prioritaire. 4. Si non-réponse sous 2 min, contacter le SAMU (15).")
                    .niveauPriorite(4)
                    .build();

            Protocol p2 = Protocol.builder()
                    .type("MALAISE")
                    .etapes("1. Communication immédiate pour évaluer la conscience. 2. Notifier les secours médicalisés d'urgence (SAMU 15). 3. Informer le contact d'urgence et transmettre le dossier médical.")
                    .niveauPriorite(5)
                    .build();

            Protocol p3 = Protocol.builder()
                    .type("URGENCE_CARDIAQUE")
                    .etapes("1. Alerte maximale : Déclenchement automatique SAMU (15). 2. Dépêcher l'assistant d'astreinte équipé de défibrillateur (DAE). 3. Notifier la famille.")
                    .niveauPriorite(5)
                    .build();

            Protocol p4 = Protocol.builder()
                    .type("INTRUSION")
                    .etapes("1. Déclencher signal sonore dissuasif. 2. Vérifier sécurité du bénéficiaire. 3. Contacter les forces de l'ordre (17) et le contact référent.")
                    .niveauPriorite(3)
                    .build();

            protocolRepository.saveAll(List.of(p1, p2, p3, p4));
            log.info("4 protocoles d'urgence initialisés avec succès.");
        }

    }
}
