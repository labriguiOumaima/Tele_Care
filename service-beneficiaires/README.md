# service-beneficiaires

Microservice du projet **Tele-Care** (assistance à domicile pour personnes âgées).
Il gère les **profils des bénéficiaires**, leurs **contacts d’urgence** et la consultation de leur **dossier complet**.

| Élément | Valeur |
|---|---|
| Port | 8081 |
| Base de données | H2 persistante (`beneficiaires`), propre au service |
| Avancement actuel | CRUD, DTO validés, dossier complet et collection Postman |
| Eureka | Client configuré ; enregistrement à vérifier avec le serveur |

## 1. Prérequis

| Outil | Version ou utilisation |
|---|---|
| Java (JDK) | 17 |
| Maven | Wrapper `mvnw` fourni |
| Spring Boot | 4.1.2 |
| Spring Cloud | 2025.1.4 |
| Postman | Importer et rejouer les requêtes sauvegardées |
| Serveur Eureka | Attendu sur le port 8761 pour vérifier l’enregistrement |

Le service est configuré comme client Eureka.
Sans serveur disponible sur le port 8761, des messages d’échec de connexion sont attendus.
Les API restent accessibles directement sur le port 8081.

## 2. Architecture du service

Architecture en couches **Controller → Service → Repository**, avec des **entités JPA**, des **DTO validés** (`@Valid`) et un **mapping manuel**.

Le service possède sa **propre base de données** (*Database per Service*).
Les autres services utilisent ses API pour consulter les bénéficiaires et leurs contacts.

Package principal : `com.telecare.beneficiaires.servicebeneficiaires`.

| Package | Classes |
|---|---|
| `controller` | `BeneficiaryController`, `ContactController` |
| `service` | `BeneficiaryService`, `ContactService` |
| `repository` | `BeneficiaryRepository`, `ContactRepository` |
| `entity` | `Beneficiary`, `Contact` |
| `dto` | `BeneficiaryRequest`, `BeneficiaryResponse`, `ContactRequest`, `ContactResponse`, `BeneficiaryDossierResponse` |

## 3. Modèle de données

### Beneficiary

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré automatiquement |
| `nom` | `String` | Nom du bénéficiaire |
| `age` | `Integer` | Âge du bénéficiaire |
| `adresse` | `String` | Adresse du domicile |
| `pathologie` | `String` | Informations sur la pathologie |
| `telephone` | `String` | Numéro de téléphone |

### Contact

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré automatiquement |
| `beneficiaryId` | `Long` | Référence locale vers un bénéficiaire du même service |
| `nom` | `String` | Nom du contact d’urgence |
| `lien` | `String` | Lien avec le bénéficiaire |
| `telephone` | `String` | Numéro de téléphone |

Un bénéficiaire peut posséder **zéro à plusieurs contacts**.
Chaque contact appartient à **un bénéficiaire**.

### Validation et règles de gestion

| Action ou donnée | Condition | Sinon |
|---|---|---|
| Nom du bénéficiaire | Obligatoire et non vide | `400` |
| Âge | Obligatoire, compris entre 0 et 150 ans | `400` |
| Adresse et téléphone du bénéficiaire | Obligatoires et non vides | `400` |
| Pathologie | Facultative | — |
| Identifiant du bénéficiaire dans une requête de contact | Obligatoire et strictement positif | `400` |
| Nom, lien et téléphone du contact | Obligatoires et non vides | `400` |
| Créer un contact | Le bénéficiaire existe | `404` |
| Modifier un contact | Le contact et le bénéficiaire demandé existent | `404` |
| Consulter, modifier ou supprimer une ressource | La ressource existe | `404` |
| Supprimer un bénéficiaire | Aucun contact ne lui est rattaché | `409` |

## 4. API REST

### Bénéficiaires

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `GET` | `/beneficiaires` | Lister les bénéficiaires | `200` |
| `GET` | `/beneficiaires/{id}` | Consulter un bénéficiaire | `200` |
| `POST` | `/beneficiaires` | Ajouter un bénéficiaire | `201` + `Location` |
| `PUT` | `/beneficiaires/{id}` | Modifier un bénéficiaire | `200` |
| `DELETE` | `/beneficiaires/{id}` | Supprimer un bénéficiaire sans contacts | `204` |
| `GET` | `/beneficiaires/{id}/dossier` | Consulter le bénéficiaire et ses contacts | `200` |

Exemple de création avec `POST /beneficiaires` :

```json
{
  "nom": "Fatima",
  "age": 75,
  "adresse": "Casablanca",
  "pathologie": "Diabète",
  "telephone": "0600000000"
}
```

### Contacts

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `POST` | `/contacts` | Ajouter un contact | `201` + `Location` |
| `GET` | `/contacts/{id}` | Consulter un contact | `200` |
| `GET` | `/beneficiaires/{beneficiaryId}/contacts` | Lister les contacts d’un bénéficiaire | `200` |
| `PUT` | `/contacts/{id}` | Modifier un contact | `200` |
| `DELETE` | `/contacts/{id}` | Supprimer un contact | `204` |

Exemple de création avec `POST /contacts` :

```json
{
  "beneficiaryId": 4,
  "nom": "Sara",
  "lien": "Fille",
  "telephone": "0611111111"
}
```

Remplacer `4` par l’identifiant d’un bénéficiaire existant.

### Dossier complet

L’endpoint `GET /beneficiaires/{id}/dossier` retourne un DTO composé :

| Champ | Contenu |
|---|---|
| `beneficiaire` | Informations du bénéficiaire |
| `contacts` | Liste de ses contacts d’urgence |

Si le bénéficiaire existe sans contact, la liste `contacts` est vide.
Si le bénéficiaire n’existe pas, l’API renvoie `404`.

### Codes d’erreur

| Code | Signification |
|---|---|
| `400` | Données invalides |
| `404` | Bénéficiaire ou contact introuvable |
| `409` | Suppression refusée : le bénéficiaire possède encore des contacts |

Les codes HTTP sont gérés.
La personnalisation centralisée des messages d’erreur reste à réaliser.

## 5. Lancer le service

Depuis le dossier `service-beneficiaires` :

```powershell
.\mvnw.cmd spring-boot:run
```

Le service peut également être lancé depuis IntelliJ en exécutant `ServiceBeneficiairesApplication`.

| Vérification | Adresse ou résultat attendu |
|---|---|
| État de santé | http://localhost:8081/actuator/health |
| Statut attendu | `"status": "UP"` |
| Liste des bénéficiaires | http://localhost:8081/beneficiaires |

### Accès à la base H2

| Paramètre | Valeur |
|---|---|
| Console | http://localhost:8081/h2-console |
| JDBC URL | `jdbc:h2:file:./data/beneficiaires` |
| User Name | `sa` |
| Password | Vide |
| Tables | `BENEFICIARIES`, `CONTACTS` |

La base est enregistrée dans le dossier local `data/`.
Les données sont conservées après un redémarrage.

### Configuration Eureka Client

La dépendance `spring-cloud-starter-netflix-eureka-client` est présente dans le `pom.xml`.
Elle active le client Eureka sans annotation supplémentaire sur la classe principale.

Configuration dans `application.properties` :

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.instance-id=${eureka.instance.hostname}:${spring.application.name}:${server.port}
```

| Élément | Valeur |
|---|---|
| Nom du service | `service-beneficiaires` |
| Nom attendu dans Eureka | `SERVICE-BENEFICIAIRES` |
| Port du service | 8081 |
| Serveur attendu | http://localhost:8761 |
| Identifiant d’instance | `localhost:service-beneficiaires:8081` |
| État actuel | Client configuré ; enregistrement non encore vérifié |

`localhost` désigne le PC qui exécute le service.
Si Eureka est hébergé sur un autre ordinateur, adapter son adresse.

Pour vérifier l’intégration, démarrer Eureka puis ce service.
L’application doit apparaître dans le registre avec le statut `UP`.

L’accès via la Gateway reste à vérifier une fois celle-ci disponible.
Avec le routage dynamique prévu, l’URL attendue est :

```text
http://localhost:8080/service-beneficiaires/beneficiaires
```

## 6. Tests

La collection Postman exportée se trouve dans le dossier `postman/`.

| Étape | Action |
|---|---|
| 1 | Démarrer le service sur le port 8081 |
| 2 | Dans Postman, cliquer sur **Import** et sélectionner le fichier JSON |
| 3 | Créer un bénéficiaire et noter son identifiant |
| 4 | Adapter les requêtes suivantes avec cet identifiant |
| 5 | Créer un contact et noter son identifiant |
| 6 | Exécuter les consultations, modifications et cas d’erreur |
| 7 | Tester la suppression bloquée avant de supprimer le contact |
| 8 | Supprimer le contact, puis le bénéficiaire |

### Vérifications manuelles réalisées

| Catégorie | Scénarios |
|---|---|
| Bénéficiaires | Création, consultation et modification |
| Validation | Refus d’un nom vide avec `400` |
| Ressource inexistante | Bénéficiaire introuvable avec `404` |
| Contacts | Création, liste par bénéficiaire et modification |
| Règle de suppression | Refus de supprimer un bénéficiaire possédant un contact avec `409` |
| Suppression d’un contact | Réponse `204`, puis consultation avec `404` |
| Dossier complet | Consultation du bénéficiaire et de ses contacts dans une seule réponse |

Les requêtes sont sauvegardées et les résultats ont été vérifiés manuellement.
Aucune assertion automatique Postman n’a encore été ajoutée.

Les identifiants sont générés automatiquement : adapter les identifiants des URL et des corps JSON avant de rejouer les requêtes.
La collection ne prépare pas automatiquement les données de test.

## 7. Avancement par jalon

La numérotation ci-dessous suit les jalons du PFM.

| Jalon | Contenu pour service-beneficiaires | État |
|---|---|---|
| **V1** | CRUD des bénéficiaires et contacts, couches séparées, DTO validés, base propre, codes HTTP et collection Postman | Implémenté |
| **V2** | Enregistrement Eureka et accès via la Gateway dynamique | Client configuré ; enregistrement `UP` et accès Gateway à vérifier |
| **V3** | Chargement de la configuration depuis Config Server | À venir |
| **V4** | Fournir les API bénéficiaire, contacts et dossier complet pour les appels OpenFeign | API disponibles ; intégration à vérifier |
| **V5** | Fournir les coordonnées utiles aux notifications RabbitMQ ; envoi assuré par service-notification | Intégration à venir |
| **V6** | Gestion centralisée des erreurs et idempotence des créations ; résilience selon les appels interservices | À venir |
| **V7** | Publication fiable des événements du référentiel pour alimenter les projections locales via Kafka | À venir |

## 8. Périmètre

Ce service correspond à la **Personne 1** du groupe.

| Responsabilité | Application |
|---|---|
| Profils bénéficiaires et contacts d’urgence | `service-beneficiaires` |
| Déclenchement des alertes et protocoles | `service-alertes` |
| Affectation et suivi des interventions | `service-interventions` |
| Référentiel des assistants | `service-utilisateurs` |
| Envoi des notifications | `service-notification` |
| Découverte des services | Eureka Server |
| Routage des requêtes | API Gateway |
| Configuration centralisée | Config Server |

Le contrôle des dépendances distantes, notamment des alertes avant la suppression d’un bénéficiaire, reste à compléter lors de l’intégration.# service-beneficiaires

Microservice du projet **Tele-Care** (assistance à domicile pour personnes âgées).
Il gère les **profils des bénéficiaires**, leurs **contacts d’urgence** et la consultation de leur **dossier complet**.

| Élément | Valeur |
|---|---|
| Port | 8081 |
| Base de données | H2 persistante (`beneficiaires`), propre au service |
| Avancement actuel | CRUD, DTO validés, dossier complet et collection Postman |
| Eureka | Client configuré ; enregistrement à vérifier avec le serveur |

## 1. Prérequis

| Outil | Version ou utilisation |
|---|---|
| Java (JDK) | 17 |
| Maven | Wrapper `mvnw` fourni |
| Spring Boot | 4.1.2 |
| Spring Cloud | 2025.1.4 |
| Postman | Importer et rejouer les requêtes sauvegardées |
| Serveur Eureka | Attendu sur le port 8761 pour vérifier l’enregistrement |

Le service est configuré comme client Eureka.
Sans serveur disponible sur le port 8761, des messages d’échec de connexion sont attendus.
Les API restent accessibles directement sur le port 8081.

## 2. Architecture du service

Architecture en couches **Controller → Service → Repository**, avec des **entités JPA**, des **DTO validés** (`@Valid`) et un **mapping manuel**.

Le service possède sa **propre base de données** (*Database per Service*).
Les autres services utilisent ses API pour consulter les bénéficiaires et leurs contacts.

Package principal : `com.telecare.beneficiaires.servicebeneficiaires`.

| Package | Classes |
|---|---|
| `controller` | `BeneficiaryController`, `ContactController` |
| `service` | `BeneficiaryService`, `ContactService` |
| `repository` | `BeneficiaryRepository`, `ContactRepository` |
| `entity` | `Beneficiary`, `Contact` |
| `dto` | `BeneficiaryRequest`, `BeneficiaryResponse`, `ContactRequest`, `ContactResponse`, `BeneficiaryDossierResponse` |

## 3. Modèle de données

### Beneficiary

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré automatiquement |
| `nom` | `String` | Nom du bénéficiaire |
| `age` | `Integer` | Âge du bénéficiaire |
| `adresse` | `String` | Adresse du domicile |
| `pathologie` | `String` | Informations sur la pathologie |
| `telephone` | `String` | Numéro de téléphone |

### Contact

| Attribut | Type | Description |
|---|---|---|
| `id` | `Long` | Identifiant généré automatiquement |
| `beneficiaryId` | `Long` | Référence locale vers un bénéficiaire du même service |
| `nom` | `String` | Nom du contact d’urgence |
| `lien` | `String` | Lien avec le bénéficiaire |
| `telephone` | `String` | Numéro de téléphone |

Un bénéficiaire peut posséder **zéro à plusieurs contacts**.
Chaque contact appartient à **un bénéficiaire**.

### Validation et règles de gestion

| Action ou donnée | Condition | Sinon |
|---|---|---|
| Nom du bénéficiaire | Obligatoire et non vide | `400` |
| Âge | Obligatoire, compris entre 0 et 150 ans | `400` |
| Adresse et téléphone du bénéficiaire | Obligatoires et non vides | `400` |
| Pathologie | Facultative | — |
| Identifiant du bénéficiaire dans une requête de contact | Obligatoire et strictement positif | `400` |
| Nom, lien et téléphone du contact | Obligatoires et non vides | `400` |
| Créer un contact | Le bénéficiaire existe | `404` |
| Modifier un contact | Le contact et le bénéficiaire demandé existent | `404` |
| Consulter, modifier ou supprimer une ressource | La ressource existe | `404` |
| Supprimer un bénéficiaire | Aucun contact ne lui est rattaché | `409` |

## 4. API REST

### Bénéficiaires

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `GET` | `/beneficiaires` | Lister les bénéficiaires | `200` |
| `GET` | `/beneficiaires/{id}` | Consulter un bénéficiaire | `200` |
| `POST` | `/beneficiaires` | Ajouter un bénéficiaire | `201` + `Location` |
| `PUT` | `/beneficiaires/{id}` | Modifier un bénéficiaire | `200` |
| `DELETE` | `/beneficiaires/{id}` | Supprimer un bénéficiaire sans contacts | `204` |
| `GET` | `/beneficiaires/{id}/dossier` | Consulter le bénéficiaire et ses contacts | `200` |

Exemple de création avec `POST /beneficiaires` :

```json
{
  "nom": "Fatima",
  "age": 75,
  "adresse": "Casablanca",
  "pathologie": "Diabète",
  "telephone": "0600000000"
}
```

### Contacts

| Méthode | URL | Rôle | Succès |
|---|---|---|---|
| `POST` | `/contacts` | Ajouter un contact | `201` + `Location` |
| `GET` | `/contacts/{id}` | Consulter un contact | `200` |
| `GET` | `/beneficiaires/{beneficiaryId}/contacts` | Lister les contacts d’un bénéficiaire | `200` |
| `PUT` | `/contacts/{id}` | Modifier un contact | `200` |
| `DELETE` | `/contacts/{id}` | Supprimer un contact | `204` |

Exemple de création avec `POST /contacts` :

```json
{
  "beneficiaryId": 4,
  "nom": "Sara",
  "lien": "Fille",
  "telephone": "0611111111"
}
```

Remplacer `4` par l’identifiant d’un bénéficiaire existant.

### Dossier complet

L’endpoint `GET /beneficiaires/{id}/dossier` retourne un DTO composé :

| Champ | Contenu |
|---|---|
| `beneficiaire` | Informations du bénéficiaire |
| `contacts` | Liste de ses contacts d’urgence |

Si le bénéficiaire existe sans contact, la liste `contacts` est vide.
Si le bénéficiaire n’existe pas, l’API renvoie `404`.

### Codes d’erreur

| Code | Signification |
|---|---|
| `400` | Données invalides |
| `404` | Bénéficiaire ou contact introuvable |
| `409` | Suppression refusée : le bénéficiaire possède encore des contacts |

Les codes HTTP sont gérés.
La personnalisation centralisée des messages d’erreur reste à réaliser.

## 5. Lancer le service

Depuis le dossier `service-beneficiaires` :

```powershell
.\mvnw.cmd spring-boot:run
```

Le service peut également être lancé depuis IntelliJ en exécutant `ServiceBeneficiairesApplication`.

| Vérification | Adresse ou résultat attendu |
|---|---|
| État de santé | http://localhost:8081/actuator/health |
| Statut attendu | `"status": "UP"` |
| Liste des bénéficiaires | http://localhost:8081/beneficiaires |

### Accès à la base H2

| Paramètre | Valeur |
|---|---|
| Console | http://localhost:8081/h2-console |
| JDBC URL | `jdbc:h2:file:./data/beneficiaires` |
| User Name | `sa` |
| Password | Vide |
| Tables | `BENEFICIARIES`, `CONTACTS` |

La base est enregistrée dans le dossier local `data/`.
Les données sont conservées après un redémarrage.

### Configuration Eureka Client

La dépendance `spring-cloud-starter-netflix-eureka-client` est présente dans le `pom.xml`.
Elle active le client Eureka sans annotation supplémentaire sur la classe principale.

Configuration dans `application.properties` :

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.hostname=localhost
eureka.instance.instance-id=${eureka.instance.hostname}:${spring.application.name}:${server.port}
```

| Élément | Valeur |
|---|---|
| Nom du service | `service-beneficiaires` |
| Nom attendu dans Eureka | `SERVICE-BENEFICIAIRES` |
| Port du service | 8081 |
| Serveur attendu | http://localhost:8761 |
| Identifiant d’instance | `localhost:service-beneficiaires:8081` |
| État actuel | Client configuré ; enregistrement non encore vérifié |

`localhost` désigne le PC qui exécute le service.
Si Eureka est hébergé sur un autre ordinateur, adapter son adresse.

Pour vérifier l’intégration, démarrer Eureka puis ce service.
L’application doit apparaître dans le registre avec le statut `UP`.

L’accès via la Gateway reste à vérifier une fois celle-ci disponible.
Avec le routage dynamique prévu, l’URL attendue est :

```text
http://localhost:8080/service-beneficiaires/beneficiaires
```

## 6. Tests

La collection Postman exportée se trouve dans le dossier `postman/`.

| Étape | Action |
|---|---|
| 1 | Démarrer le service sur le port 8081 |
| 2 | Dans Postman, cliquer sur **Import** et sélectionner le fichier JSON |
| 3 | Créer un bénéficiaire et noter son identifiant |
| 4 | Adapter les requêtes suivantes avec cet identifiant |
| 5 | Créer un contact et noter son identifiant |
| 6 | Exécuter les consultations, modifications et cas d’erreur |
| 7 | Tester la suppression bloquée avant de supprimer le contact |
| 8 | Supprimer le contact, puis le bénéficiaire |

### Vérifications manuelles réalisées

| Catégorie | Scénarios |
|---|---|
| Bénéficiaires | Création, consultation et modification |
| Validation | Refus d’un nom vide avec `400` |
| Ressource inexistante | Bénéficiaire introuvable avec `404` |
| Contacts | Création, liste par bénéficiaire et modification |
| Règle de suppression | Refus de supprimer un bénéficiaire possédant un contact avec `409` |
| Suppression d’un contact | Réponse `204`, puis consultation avec `404` |
| Dossier complet | Consultation du bénéficiaire et de ses contacts dans une seule réponse |

Les requêtes sont sauvegardées et les résultats ont été vérifiés manuellement.
Aucune assertion automatique Postman n’a encore été ajoutée.

Les identifiants sont générés automatiquement : adapter les identifiants des URL et des corps JSON avant de rejouer les requêtes.
La collection ne prépare pas automatiquement les données de test.

## 7. Avancement par jalon

La numérotation ci-dessous suit les jalons du PFM.

| Jalon | Contenu pour service-beneficiaires | État |
|---|---|---|
| **V1** | CRUD des bénéficiaires et contacts, couches séparées, DTO validés, base propre, codes HTTP et collection Postman | Implémenté |
| **V2** | Enregistrement Eureka et accès via la Gateway dynamique | Client configuré ; enregistrement `UP` et accès Gateway à vérifier |
| **V3** | Chargement de la configuration depuis Config Server | À venir |
| **V4** | Fournir les API bénéficiaire, contacts et dossier complet pour les appels OpenFeign | API disponibles ; intégration à vérifier |
| **V5** | Fournir les coordonnées utiles aux notifications RabbitMQ ; envoi assuré par service-notification | Intégration à venir |
| **V6** | Gestion centralisée des erreurs et idempotence des créations ; résilience selon les appels interservices | À venir |
| **V7** | Publication fiable des événements du référentiel pour alimenter les projections locales via Kafka | À venir |

## 8. Périmètre

Ce service correspond à la **Personne 1** du groupe.

| Responsabilité | Application |
|---|---|
| Profils bénéficiaires et contacts d’urgence | `service-beneficiaires` |
| Déclenchement des alertes et protocoles | `service-alertes` |
| Affectation et suivi des interventions | `service-interventions` |
| Référentiel des assistants | `service-utilisateurs` |
| Envoi des notifications | `service-notification` |
| Découverte des services | Eureka Server |
| Routage des requêtes | API Gateway |
| Configuration centralisée | Config Server |

Le contrôle des dépendances distantes, notamment des alertes avant la suppression d’un bénéficiaire, reste à compléter lors de l’intégration.