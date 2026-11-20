# service-notification

Microservice de notification de TeleCare.
Il prévient le contact de confiance d'un bénéficiaire quand une intervention est affectée ou clôturée.

## Fonctionnement

- Consomme deux topics Kafka : `intervention-assigned` et `emergency-resolved` (publiés par service-interventions).
- Appelle service-beneficiaires (Feign, avec Retry, Circuit Breaker et Bulkhead) pour trouver le contact.
- Enregistre chaque notification en base (H2) et la trace dans les logs.
- Idempotence : un même `eventId` n'est traité qu'une seule fois.
- En cas d'échec, le consommateur réessaie 3 fois (2 s d'intervalle), sauf si le bénéficiaire est introuvable.

## API

| Méthode | URL | Rôle |
|---|---|---|
| GET | `/notifications` | Liste des notifications (filtre optionnel `?alertId=`) |
| GET | `/notifications/{id}` | Détail d'une notification |

## Configuration

Le fichier de configuration est `config-repo/service-notification.properties` (port 8085).

## Prérequis au lancement

Kafka (localhost:9092), Eureka (8761), Config Server, service-beneficiaires.

## Lancement

    .\mvnw.cmd spring-boot:run