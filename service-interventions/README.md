# service-interventions

Microservice du projet **Tele-Care** (assistance à domicile pour personnes âgées).
Il gère la **prise en charge terrain** d'une urgence : affectation d'un assistant d'astreinte, heure d'arrivée, compte-rendu et clôture.

| | |
|---|---|
| **Port** | 8083 |
| **Base de données** | H2 en mémoire (`interventionsdb`), propre au service |
| **Jalon livré** | **v4.0** — Communication synchrone (OpenFeign) : vérification des références distantes à la création et consultation enrichie (après v1.0 : services métier, v2.0 : client Eureka, v3.0 : Config Server) |

---

## 1. Prérequis

| Outil | Version |
|---|---|
| Java (JDK) | 17 |
| Maven | Non requis, le wrapper `mvnw` est fourni |
| Spring Boot | 3.5.6 |
| Spring Cloud (BOM) | 2025.0.0 (clients Eureka, Config et OpenFeign) |
| Postman | Pour rejouer la collection de tests |
| Serveur Eureka | Port 8761. Sans lui, le service démarre quand même (avertissements de connexion dans la console) |
| Config Server | Port 8888. **Sans lui, le service refuse de démarrer** (comportement voulu, voir 5.2) |
| service-alertes | Port 8082, appelé par OpenFeign (voir 5.3). Sans lui, la création et la consultation enrichie répondent `503` |

## 2. Architecture du service

```mermaid
flowchart LR
    Client["Client / Postman"] --> C["InterventionController<br/>/interventions"]
    C --> S["InterventionService<br/>règles métier"]
    S --> R["InterventionRepository"]
    S --> F["Clients OpenFeign<br/>AlerteRequest, ProtocolRequest"]
    F --> A["service-alertes<br/>(découvert par Eureka)"]
    R --> DB[("Base interventionsdb<br/>H2")]
```

Architecture en couches **Controller → Service → Repository → Entité**, avec des **DTO** validés (`@Valid`) et un mapping par **MapStruct**.
Le service a sa **propre base de données** (*Database per Service*) : il ne partage aucune table avec les autres services.

```
src/main/java/com/telecare/serviceinterventions
├── controller   InterventionController
├── service      InterventionService
├── repository   InterventionRepository
├── entite       Intervention, StatutIntervention
├── dto          InterventionCreationDto, InterventionModificationDto, ArriveeDto, CompteRenduDto,
│                InterventionDto, InterventionDetailDto
├── feignClient  AlerteRequest, ProtocolRequest, AlerteDto, ProtocolDto
├── mapper       InterventionMapper
└── exception    RessourceIntrouvableException, TransitionInterditeException, InterventionExistanteException,
                 GlobalExceptionHandler
```

## 3. Modèle de données

Entité **`Intervention`**, conforme au diagramme de classes du projet :

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré |
| `alertId` | `Long` | **Référence distante** vers l'alerte (service-alertes). Aucune clé étrangère, car la base est séparée. Unique : une seule intervention par alerte |
| `assistantNom` | `String` | Assistant d'astreinte affecté (obligatoire) |
| `heureArrivee` | `LocalDateTime` | Heure d'arrivée sur place |
| `compteRendu` | `String` | Compte-rendu de l'intervention (2000 caractères max) |
| `statut` | `StatutIntervention` | Stocké en texte en base |

### Cycle de vie du statut

Le cahier des charges ne fixe pas les valeurs de `statut`. Choix de conception :

```
AFFECTEE  ──(arrivée enregistrée)──▶  SUR_PLACE  ──(compte-rendu + clôture)──▶  CLOTUREE
```

| Action | Condition | Sinon |
|---|---|---|
| Créer | Aucune intervention pour cet `alertId` | `409` |
| Enregistrer l'arrivée | Statut `AFFECTEE` | `409` |
| Rédiger le compte-rendu | Statut `SUR_PLACE` | `409` |
| Clôturer | Statut `SUR_PLACE` et compte-rendu rédigé | `409` |
| Modifier l'assistant | Statut différent de `CLOTUREE` | `409` |
| Supprimer | Statut différent de `CLOTUREE` | `409` |

## 4. API REST

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `GET` | `/interventions` | Lister (filtre optionnel `?statut=SUR_PLACE`) | `200` |
| `GET` | `/interventions/{id}` | Consulter une intervention | `200` |
| `GET` | `/interventions/{id}/detail` | Consultation enrichie : intervention, alerte et protocole (OpenFeign) | `200` |
| `POST` | `/interventions/new` | Créer une intervention, après vérification de l'alerte et de son protocole | `201` + `Location` |
| `PUT` | `/interventions/{id}` | Modifier l'assistant affecté (interdit si `CLOTUREE`) | `200` |
| `PUT` | `/interventions/{id}/arrivee` | Enregistrer l'heure d'arrivée (corps facultatif, heure actuelle par défaut) | `200` |
| `PUT` | `/interventions/{id}/compte-rendu` | Rédiger le compte-rendu | `200` |
| `PUT` | `/interventions/{id}/cloture` | Clôturer l'intervention | `200` |
| `DELETE` | `/interventions/{id}` | Supprimer une intervention | `204` |

**Codes d'erreur** : `400` données invalides (validation) · `404` ressource inexistante (locale ou distante) · `409` transition de statut interdite, doublon d'`alertId` ou refus métier · `502` erreur HTTP du service distant · `503` service distant indisponible.

Exemple de création :

```json
POST /interventions/new
{
  "alertId": 42,
  "assistantNom": "Karim Benali"
}
```

## 5. Lancer le service

**Ordre de démarrage** : Eureka (8761), Config Server (8888), service-alertes (8082), puis ce service (8083).

Depuis le dossier `service-interventions` :

```powershell
.\mvnw.cmd spring-boot:run
```

Vérification : <http://localhost:8083/actuator/health> doit renvoyer `{"status":"UP"}`.

### 5.1 Enregistrement dans Eureka (jalon v2.0)

Le service est un **client Eureka** : au démarrage, il s'enregistre auprès du serveur Eureka (port 8761) pour pouvoir être découvert par la Gateway.

| Élément | Valeur |
|---|---|
| Nom dans Eureka | `SERVICE-INTERVENTIONS` (valeur de `spring.application.name`) |
| Port | 8083 |
| Identifiant d'instance | `localhost:service-interventions:8083` |
| Statut attendu | `UP` |

Configuration (située dans `config-repo/service-interventions.properties` depuis le jalon v3.0) :

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.instance-id=${eureka.instance.hostname}:${spring.application.name}:${server.port}
```

La dépendance `spring-cloud-starter-netflix-eureka-client` est ajoutée au `pom.xml`, et la classe principale porte `@EnableDiscoveryClient`.

**Vérification** : démarrer le serveur Eureka, puis le service, et ouvrir <http://localhost:8761>. L'application `SERVICE-INTERVENTIONS` doit apparaître avec le statut `UP`.


### 5.2 Configuration centralisée (jalon v3.0)

La configuration du service n'est plus dans le projet : elle est dans le dossier **`config-repo`**, à la racine du dépôt Git, et servie par le Config Server.

| Élément | Emplacement |
|---|---|
| Configuration du service (port, base H2, JPA, erreurs, Eureka) | `config-repo/service-interventions.properties` |
| Configuration locale | `service-interventions/src/main/resources/application.properties` : **deux lignes seulement** |

Contenu de la configuration locale :

```properties
spring.application.name=service-interventions
spring.config.import=configserver:http://localhost:8888
```

- `spring.application.name` identifie le fichier à charger côté serveur (`service-interventions.properties`).
- `spring.config.import` charge la configuration distante au démarrage.
- Sans `optional:`, le service **refuse de démarrer** si le Config Server est injoignable (comportement *fail-fast* voulu par le TP).

La dépendance `spring-cloud-starter-config` est ajoutée au `pom.xml`.

**Vérification** : ouvrir <http://localhost:8888/service-interventions/default>. La réponse doit contenir `server.port` à `8083`, `spring.datasource.url` à `jdbc:h2:mem:interventionsdb` et les propriétés Eureka. Le service démarre ensuite sans configuration locale, s'enregistre dans Eureka (`UP`), et la collection Postman reste à 42 tests réussis.

### 5.3 Communication synchrone avec OpenFeign (jalon v4.0)

Le service appelle `service-alertes` grâce à deux clients OpenFeign typés (`AlerteRequest`, `ProtocolRequest`). L'adresse n'est pas écrite en dur : le client utilise le nom `service-alertes` découvert par Eureka (`@EnableFeignClients`, avec `spring-cloud-starter-openfeign` et `spring-cloud-starter-loadbalancer`).

| Client | Appel | DTO de réponse |
|---|---|---|
| `AlerteRequest` | `GET /alertes/{id}` | `AlerteDto` (id, beneficiaryId, type, gravite, dateDeclenchement, statut) |
| `ProtocolRequest` | `GET /protocoles/type/{type}` | `ProtocolDto` (id, type, etapes, niveauPriorite) |

**Création contrôlée** (`POST /interventions/new`), dans cet ordre :

| # | Contrôle | En cas d'échec |
|---|---|---|
| 1 | Aucune intervention n'existe déjà pour cet `alertId` (**idempotence**, avant tout appel externe) | `409` |
| 2 | L'alerte existe | `404` « Ressource externe introuvable » |
| 3 | L'alerte est `DECLENCHEE` | `409` |
| 4 | Le protocole du type de l'alerte existe | `404` « Ressource externe introuvable » |
| 5 | Le type du protocole correspond au type de l'alerte | `409` |
| 6 | Création, statut `AFFECTEE` | `201` |

**Idempotence** : l'`alertId` sert de clé. Il est unique en base, et sa présence est vérifiée avant tout appel externe : une même alerte ne peut donner lieu qu'à une seule intervention.

**Gestion des erreurs de communication** (`GlobalExceptionHandler`) :

| Situation | Réponse |
|---|---|
| Ressource introuvable chez le service distant | `404` « Ressource externe introuvable » |
| Échec de communication (connexion impossible, délai dépassé) | `503` « Échec de communication avec un service » |
| Service ou instance indisponible | `503` « Service ou instance indisponible » |
| Autre erreur HTTP du service distant | `502` « Erreur HTTP du service distant : [statut] » |

**Consultation enrichie** : `GET /interventions/{id}/detail` assemble les données de l'intervention (base locale) et celles de l'alerte et de son protocole (OpenFeign) :

```json
{
  "id": 1, "alertId": 1000, "assistantNom": "Karim Benali",
  "heureArrivee": null, "compteRendu": null, "statut": "AFFECTEE",
  "alerteType": "CHUTE", "alerteGravite": "HAUTE", "alerteStatut": "DECLENCHEE",
  "protocoleEtapes": "1. Appeler les secours 2. Securiser la personne 3. Prevenir la famille",
  "protocoleNiveauPriorite": 1
}
```

## 6. Tests

La collection Postman est dans [`postman/service-interventions.postman_collection.json`](postman/service-interventions.postman_collection.json).

1. Démarrer Eureka, le Config Server et `service-alertes` (ou une simulation exposant les mêmes adresses), puis le service (port 8083).
2. Dans Postman : **Import** puis choisir le fichier de la collection.
3. Clic droit sur la collection → **Run collection**.

**38 requêtes, 57 tests automatiques, tous réussis.**

| Dossier | Couverture |
|---|---|
| 1 — Cas valides | Cycle complet : création, consultation, arrivée, compte-rendu, clôture, filtre |
| 2 — Erreurs de validation | Corps invalide, JSON cassé, statut inconnu, `assistantNom` vide (`400`) |
| 3 — Ressources inexistantes | Identifiant inexistant sur chaque endpoint (`404`) |
| 4 — Règles métier | Doublon, clôture avant arrivée, double arrivée, modification ou suppression d'une intervention clôturée (`409`), suppression valide (`204`) |
| 5 — OpenFeign | Alerte introuvable (`404`), alerte non déclenchée (`409`), protocole introuvable (`404`), protocole incompatible (`409`), erreur du service distant (`502`), consultation enrichie (`200`) et intervention inexistante (`404`) |

Les tests du dossier 5 ont été réalisés avec une simulation de `service-alertes`. Un test manuel complète la collection : `service-alertes` arrêté, `POST /interventions/new` répond `503` « Service ou instance indisponible ».

## 7. Avancement par jalon

| Jalon | Contenu | État |
|---|---|---|
| **v1.0** | Couches Controller / Service / Repository, DTO validés, base propre, API REST, gestion des erreurs, tests Postman | **Terminé** |
| **v2.0** | Client Eureka : enregistrement du service dans Eureka (statut `UP`) | **Terminé** |
| **v3.0** | Configuration centralisée : fichier dans `config-repo`, service client du Config Server | **Terminé** |
| **v4.0** | Communication synchrone : clients OpenFeign, création contrôlée, consultation enrichie, gestion des erreurs de communication | **Terminé** |
