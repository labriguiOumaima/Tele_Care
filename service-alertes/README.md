# Service Alertes et Protocoles

Port : 8082. Les donnees sont dans une base H2 propre au service. La configuration est chargee depuis Config Server (profil local par defaut).

## API

- Protocoles : POST/GET `/api/protocols`, GET/PUT/DELETE `/api/protocols/{id}`, GET `/api/protocols/type/{type}`.
- Alertes : POST/GET `/api/alerts`, GET/DELETE `/api/alerts/{id}`, PATCH `/api/alerts/{id}/status`.
- Consultation enrichie : GET `/api/alerts/{id}/enriched` retourne le vrai beneficiaire et ses vrais contacts via Feign.
- Cycle : DECLENCHEE -> PRISE_EN_CHARGE -> RESOLUE. Une repetition du meme statut est sans effet. Un evenement d'affectation retarde ne fait pas regresser une alerte resolue.

Un beneficiaire absent provoque 404 ; une panne du service distant provoque 503. Aucun profil/contact fictif ne remplace une reponse absente. Une liste vide de contacts reste vide.

Les POST acceptent `Idempotency-Key` : meme cle et meme corps = reponse initiale ; corps different = 409. La cle et la creation sont enregistrees dans la meme transaction. Sans cle, creation ordinaire.

## Echanges

`BeneficiaryClient` appelle `/beneficiaires/{id}` et `/beneficiaires/{id}/contacts` via Eureka. `ResilientBeneficiaryClient` applique Retry, Circuit Breaker et les timeouts. Les erreurs metier 404 ne sont pas reessayees comme des pannes.

Kafka : publication de `alert-triggered` apres commit ; consommation de `intervention-assigned` et `emergency-resolved`. Les dates publiees sont des chaines ISO-8601, couvertes par un test de contrat.

Les protocoles de reference sont initialises. Aucune alerte de demonstration avec une reference distante fictive n'est creee. Le controller d'injection de simulation est uniquement disponible avec le profil explicite `simulation` ; il ne constitue pas une preuve du passage dans Kafka.

## Verification

Voir [le guide commun](../LISEZMOI_CORRECTIONS.md). Le script commun lance la vraie chaine. La collection corrigee est `../postman/TeleCare-Validation.postman_collection.json` ; les collections locales de jalons sont historiques.
