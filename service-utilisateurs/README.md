> Version corrigee : consulter [LISEZMOI_CORRECTIONS](../LISEZMOI_CORRECTIONS.md) et la collection commune `../postman/TeleCare-Validation.postman_collection.json`. Les collections et indications de jalons ci-dessous sont historiques.

# service-utilisateurs

Microservice du projet **Tele-Care** (assistance à domicile pour personnes âgées).
Il gère les **profils des utilisateurs**, les **assistants d’astreinte** et la **vérification de leur statut et rôle** pour les autres microservices.

| Élément | Valeur |
|---|---|
| Port | 8084 |
| Base de données | H2 persistante (`utilisateurs`), propre au service |
| Fonctionnalités | CRUD, DTO validés, pagination et vérification synchrone |
| Tests | Collection Postman sauvegardée avec assertions automatiques |
| Eureka | Client configuré (`service-utilisateurs`), port 8761 |
| Configuration centralisée | Fichier configuré (`config-repo/service-utilisateurs.properties`) |

## 1. Prérequis

| Outil | Version ou utilisation |
|---|---|
| Java (JDK) | 17 ou 21 |
| Maven | Wrapper `mvnw` fourni |
| Spring Boot | 4.2.0 |
| Spring Cloud | 2026.0.0 |
| Postman | Importer et rejouer la collection de requêtes sauvegardées |
| Serveur Eureka | Attendu sur le port 8761 pour l'enregistrement et découverte |
| Config Server | Attendu sur le port 8888 pour la configuration externalisée |

Le service peut démarrer en autonomie avec ses valeurs par défaut.
Sans serveur Eureka ou Config Server disponible, des messages d’avertissement de connexion apparaissent en console, mais les API restent directement accessibles sur le port 8084.

## 2. Architecture du service

Architecture en couches **Controller → Service → Repository**, avec des **entités JPA**, des **DTO validés** (`@Valid`) et un **mapping dédié**.

Le service possède sa **propre base de données** (*Database per Service*).
Les autres services (notamment `service-interventions`) utilisent ses API pour vérifier l'existence, le rôle et l'état actif des assistants d'astreinte.

Package principal : `com.telecare.utilisateurs`.

| Package | Classes |
|---|---|
| `controller` | `UtilisateurController` |
| `service` | `UtilisateurService` |
| `repository` | `UtilisateurRepository` |
| `entity` | `Utilisateur` |
| `dto` | `UtilisateurRequestDto`, `UtilisateurResponseDto`, `UtilisateurPageDto`, `VerificationUtilisateurDTO` |
| `mapper` | `UtilisateurMapper` |
| `config` | `SecurityConfig` |
| `exception` | `GlobalExceptionHandler`, `UtilisateurDejaExistantException` |

## 3. Modèle de données

### Utilisateur

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré automatiquement |
| `nom` | `String` | Nom complet de l'utilisateur |
| `email` | `String` | Adresse email unique servant d'identifiant métier |
| `role` | `String` | Rôle de l'utilisateur (`ASSISTANT`, `ADMIN`, etc.) |
| `actif` | `boolean` | Statut du compte (activé par défaut) |

### Validation et règles de gestion

| Action ou donnée | Condition | Sinon |
|---|---|---|
| Nom de l'utilisateur | Obligatoire et non vide | `400` |
| Email | Obligatoire et format email valide | `400` |
| Unicité de l'email | L'adresse email ne doit pas déjà exister en base | `409` |
| Rôle | Obligatoire et non vide | `400` |
| Consulter, modifier ou supprimer un utilisateur | L'utilisateur demandé doit exister | `404` |
| Vérification inter-service | L'ID et le rôle attendu sont transmis | `200` avec rapport de vérification |

## 4. API REST

Toutes les routes sont préfixées par `/api/utilisateurs`.

### Utilisateurs & Assistants

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `POST` | `/api/utilisateurs` | Créer un utilisateur / assistant | `201` + `Location` |
| `GET` | `/api/utilisateurs?page=0&taille=10` | Lister les utilisateurs avec pagination | `200` |
| `GET` | `/api/utilisateurs/{id}` | Consulter un utilisateur par son identifiant | `200` |
| `PUT` | `/api/utilisateurs/{id}` | Modifier les informations d'un utilisateur | `200` |
| `DELETE` | `/api/utilisateurs/{id}` | Supprimer un utilisateur | `204` |
| `PATCH` | `/api/utilisateurs/{id}/actif?actif=false` | Activer ou désactiver un utilisateur | `200` |
| `GET` | `/api/utilisateurs/{id}/verification?role=ASSISTANT` | Vérifier l'existence, le rôle et le statut actif | `200` |

Exemple de création avec `POST /api/utilisateurs` :

```json
{
  "nom": "Sara Benali",
  "email": "sara.benali@telecare.ma",
  "role": "ASSISTANT"
}
```

### Vérification inter-service (OpenFeign)

L’endpoint `GET /api/utilisateurs/{id}/verification?role=ASSISTANT` retourne un DTO structuré consommé directement par `service-interventions` :

| Champ | Contenu |
|---|---|
| `utilisateurId` | Identifiant vérifié |
| `existe` | `true` si présent en base |
| `actif` | `true` si le compte est actif |
| `roleValide` | `true` si le rôle correspond exactement à celui attendu |
| `role` | Rôle réel de l'utilisateur en base |
| `message` | Message explicatif (`Utilisateur trouve` ou `Utilisateur introuvable`) |

### Codes d’erreur

| Code | Signification |
|---|---|
| `400` | Données de requête invalides (nom manquant, email incorrect) |
| `401` | Non authentifié (sur les routes administratives protégées) |
| `404` | Utilisateur introuvable |
| `409` | Conflit d'unicité : un utilisateur avec cet email existe déjà |

Les erreurs sont centralisées et formatées par `GlobalExceptionHandler` (`@RestControllerAdvice`).

## 5. Lancer le service

Depuis le dossier `service-utilisateurs` :

```powershell
.\mvnw.cmd spring-boot:run
```

Le service peut également être lancé depuis IntelliJ en exécutant `ServiceUtilisateursApplication`.

| Vérification | Adresse ou résultat attendu |
|---|---|
| État de santé | http://localhost:8084/actuator/health |
| Statut attendu | `"status": "UP"` |
| Liste paginée | http://localhost:8084/api/utilisateurs?page=0&taille=10 |

### Accès à la base H2

| Paramètre | Valeur |
|---|---|
| Console | http://localhost:8084/h2-console |
| JDBC URL | `jdbc:h2:file:./data/utilisateurs` |
| User Name | `sa` |
| Password | *(laisser vide)* |
| Table | `UTILISATEURS` |

La base est stockée dans le dossier local `data/`. Les données sont persistantes entre les redémarrages.

## 6. Intégration Eureka et Gateway — V2

La dépendance `spring-cloud-starter-netflix-eureka-client` est activée dans le projet avec l'annotation `@EnableDiscoveryClient`.

Configuration :

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.instance-id=${eureka.instance.hostname}:${spring.application.name}:${server.port}
```

| Élément | Valeur |
|---|---|
| Nom du service | `service-utilisateurs` |
| Nom attendu dans Eureka | `SERVICE-UTILISATEURS` |
| Port du service | 8084 |
| Serveur attendu | http://localhost:8761 |
| Identifiant d’instance | `localhost:service-utilisateurs:8084` |
| État actuel | Enregistré et opérationnel (`UP`) |

Avec le routage dynamique de l'API Gateway, l’URL d'accès unifiée est :

```text
http://localhost:8080/service-utilisateurs/api/utilisateurs
```

## 7. Configuration centralisée — V3

Le fichier de configuration externalisé est placé dans le dossier `config-repo/` du dépôt commun :

```text
config-repo/service-utilisateurs.properties
```

| Élément | État actuel |
|---|---|
| Fichier spécifique au service | Configuré et présent dans `config-repo/` |
| Paramètres centralisés | Port (8084), base H2, JPA, Eureka, Actuator et sécurité |
| Configuration locale (`src/main/resources`) | Réduite au nom de service et import du Config Server |
| Import Config Server | `spring.config.import=optional:configserver:http://localhost:8888` |
| Chargement distant | Prêt |

Pour vérifier la configuration fournie par le Config Server (port 8888) :

```text
GET http://localhost:8888/service-utilisateurs/default
```

## 8. Tests

La collection Postman complète et exportée se trouve dans :
`postman/service-utilisateurs-v1.postman_collection.json`.

| Étape | Action |
|---|---|
| 1 | Démarrer le service sur le port 8084 |
| 2 | Dans Postman, cliquer sur **Import** et sélectionner le fichier JSON |
| 3 | Vérifier les variables de collection (`baseUrl`: `http://localhost:8084`, `username`: `admin`, `password`: `admin123`) |
| 4 | Lancer la collection complète via le **Collection Runner** |

### Scénarios de test automatisés

| # | Requête | Objectif du test | Code attendu |
|---|---|---|---|
| 1 | `POST /api/utilisateurs` | Création d'un assistant et stockage de l'ID | `201 Created` |
| 2 | `GET /api/utilisateurs` | Vérification de la structure paginée | `200 OK` |
| 3 | `GET /api/utilisateurs/{id}` | Consultation par identifiant | `200 OK` |
| 4 | `PUT /api/utilisateurs/{id}` | Modification des données du profil | `200 OK` |
| 5 | `GET /.../verification` | Validation de l'assistant (actif + rôle `ASSISTANT`) | `200 OK` |
| 6 | `PATCH /.../actif=false` | Désactivation du compte assistant | `200 OK` |
| 7 | `GET /.../verification` | Vérification de détection du compte inactif | `200 OK` |
| 8 | `PATCH /.../actif=true` | Réactivation du compte | `200 OK` |
| 9 | `POST /api/utilisateurs` | Test d'unicité avec email doublon | `409 Conflict` |
| 10 | `GET /api/utilisateurs/99999` | Test de ressource introuvable | `404 Not Found` |
| 11 | `DELETE /api/utilisateurs/{id}`| Suppression définitive de l'utilisateur | `204 No Content` |

Tous les tests intègrent des scripts d'assertion automatiques `pm.test` vérifiant les statuts et les charges utiles JSON.

## 9. Avancement par jalon

La numérotation suit la grille d'évaluation du contrôle :

| Jalon | Contenu pour service-utilisateurs | État |
|---|---|---|
| **V0** | Architecture, diagrammes de classes, modèle de données et documentation exhaustive | Réalisé |
| **V1** | Architecture en couches, DTO validés, base H2 isolée, CRUD autonome et collection Postman | Réalisé |
| **V2** | Enregistrement dynamique dans Eureka Server (statut `UP`) et routage Gateway | Réalisé |
| **V3** | Externalisation de la configuration dans Config Server (`service-utilisateurs.properties`) | Réalisé |
| **V4** | Fourniture des endpoints de vérification typés pour le client Feign de `service-interventions` | Réalisé |
| **V5** | Notification asynchrone (gérée dans le flux global par `service-interventions` et `service-notification`) | Réalisé (Projet) |
| **V6** | Gestion centralisée des erreurs (`@RestControllerAdvice`) et idempotence des créations | Réalisé |

## 10. Périmètre

Ce service correspond à la **Personne 4** du groupe.

| Responsabilité | Application |
|---|---|
| Profils bénéficiaires et contacts d’urgence | `service-beneficiaires` |
| Déclenchement des alertes et protocoles | `service-alertes` |
| Affectation et suivi des interventions | `service-interventions` |
| Référentiel des assistants et utilisateurs | `service-utilisateurs` |
| Envoi des notifications | `service-notification` |
| Découverte des services | Eureka Server |
| Routage des requêtes | API Gateway |
| Configuration centralisée | Config Server |
