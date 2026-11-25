# Tele-Care : corrections et verification automatique

Cette copie contient les sources corrigees apres V6. Elle ne deplace aucun tag existant et ne publie rien sur GitHub.

## Lancer sur Windows

1. Extraire ce dossier dans un nouveau repertoire, par exemple `E:\Tele_Care-corrige`.
2. Installer/utiliser un **JDK complet 17 ou 21**, Python 3.10+ et Docker Desktop avec Docker Compose v2. Un JRE seul ne suffit pas pour compiler. `java -version` doit correspondre au JDK selectionne par Maven.
3. Arreter les anciennes instances de Tele-Care qui utilisent les ports 8080 a 8085, 8761 et 8888. Ne pas lancer les applications manuellement pendant le script.
4. Ouvrir un terminal PowerShell dans le dossier extrait et executer :

```powershell
powershell -ExecutionPolicy Bypass -File .\Verifier-TeleCare.ps1 -KeepRunning
```

Le script compile et execute les tests Maven, lance Kafka et Mailpit avec Docker, demarre les huit applications dans l'ordre et execute les scenarios HTTP/Kafka/SMTP. Le premier telechargement des dependances et images peut prendre plusieurs minutes. Les rapports sont dans `reports/`, les journaux dans `.runtime/`.

Sans `-KeepRunning`, les applications Java lancees par le script sont arretees a la fin. Kafka et Mailpit restent disponibles pour consulter les mails. Pour arreter les applications laissees en marche :

```powershell
python scripts/telecare.py stop
```

Pour arreter aussi les conteneurs de cette copie, sans effacer leurs volumes :

```powershell
docker compose -p telecare-validation down
```

Autres commandes :

```powershell
python scripts/telecare.py build
python scripts/telecare.py start --skip-build
python scripts/telecare.py test
```

`test` sur une pile deja lancee execute les scenarios normaux, sans arreter les services. Les scenarios de panne et de redemarrage sont executes par `all`, qui controle les processus qu'il a lances. `--external-infra` permet d'utiliser un Kafka et un Mailpit deja disponibles ; cette option ne lance aucun conteneur.

## Ce qui est corrige

- Les huit modules utilisent Spring Boot 3.5.6, Spring Cloud 2025.0.0 et des sources Java 17 pour une base coherente avec la version demandee en cours. Un POM racine permet une seule compilation.
- Les URLs Feign correspondent aux controllers reels. Les beneficiaires inexistants donnent une erreur : aucun beneficiaire ni contact fictif ne remplace les donnees manquantes.
- Les appels Feign resilients sont dans des beans separes. Les reponses 404 ne sont pas retentees comme des pannes ; les indisponibilites produisent des reponses bornees.
- Les alertes de demonstration avec des beneficiaires inexistants ne sont plus inserees automatiquement. Les protocoles de reference restent initialises.
- Les consumers Kafka sont actifs, les types des messages sont explicites et les trois topics sont declares. La publication d'une alerte attend le commit de la base. Les logs de succes n'annoncent une publication qu'apres confirmation de Kafka.
- L'assistant est choisi parmi les vrais utilisateurs actifs de role `ASSISTANT`, et non parmi les identifiants arbitraires 1 a 4.
- Les evenements repetes ne font pas regresser une alerte resolue. Les notifications sont dedupliquees par `eventId`.
- Une adresse `email` optionnelle est ajoutee aux contacts. Pour recevoir une notification, au moins un contact du beneficiaire doit avoir un email valide.
- Notifications utilise `JavaMailSender` : un vrai message SMTP est envoye et une notification n'est pas enregistree comme envoyee si SMTP echoue.
- Les POST avec `Idempotency-Key` enregistrent la requete et la reponse en base, dans la meme transaction que la creation. Une repetition renvoie la reponse originale ; une reutilisation avec un corps different donne 409. Les cles restent valides apres redemarrage. Sans cle, un POST conserve le comportement de creation ordinaire. Les corps sont compares octet par octet via leur empreinte SHA-256 : garder le meme JSON pour une repetition.
- Les tests de contexte sont isoles du Config Server, de Kafka et d'Eureka. Les anciennes fixtures simulees sont uniquement dans les tests.

## Configuration et adresses

Le script utilise le **Config Server en profil native** pour lire le `config-repo` de cette copie. Il teste donc le code local avant tout push. Le mode Git original reste disponible en lancant Config Server sans le profil native : il lit alors la branche `main` configuree dans `application.yaml`.

| Composant | Adresse |
|---|---|
| Eureka | http://localhost:8761 |
| Config Server | http://localhost:8888 |
| API Gateway | http://localhost:8080 |
| Beneficiaires | http://localhost:8081/beneficiaires |
| Alertes | http://localhost:8082/api/alerts |
| Interventions | http://localhost:8083/interventions |
| Utilisateurs | http://localhost:8084/api/utilisateurs |
| Notifications | http://localhost:8085/notifications |
| Mails recus (Mailpit) | http://localhost:8025 |
| Kafka UI | http://localhost:8090 |

Par defaut, les ecritures Utilisateurs utilisent HTTP Basic `admin` / `admin123` pour la demonstration locale. Les variables d'environnement `SERVICE_UTILISATEURS_USERNAME` et `SERVICE_UTILISATEURS_PASSWORD` peuvent les remplacer ; le script utilise les memes valeurs.

Mailpit recoit de vrais messages SMTP en local et permet de les montrer au professeur. **Aucun mail n'est delivre dans Gmail/Outlook par ce mode de test.** Pour une livraison externe, configurer un compte SMTP autorise via `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS` et `MAIL_FROM`. Ne pas enregistrer les secrets dans Git.

## Preuves et limites

- Le bilan reel de cette execution est dans `reports/VALIDATION.md` et `reports/test-results.json`.
- `reports/Tele-Care-observed.postman_collection.json` contient les requetes/reponses observees avec les IDs du dernier test : c'est une trace avec exemples, pas un remplacement des attentes asynchrones et arrets de services realises par le script.
- `postman/TeleCare-Validation.postman_collection.json` contient un parcours reutilisable avec variables et assertions. Pour le cycle Kafka, utiliser le Runner avec un delai d'environ 1500 ms et relancer la consultation si l'evenement n'est pas encore arrive.
- Les scenarios de panne executent de vrais arrets/redemarrages. Les tests de retry unitaires comptent les appels du client simule pour verifier l'interception AOP.
- Une classe et une annotation presentes ne constituent pas une validation de production. Le rapport indique precisement ce qui a ete execute. Le lanceur Docker lui-meme doit etre execute sur le PC Windows pour verifier ses ports et Docker Desktop.
- La publication Kafka apres commit n'est pas un outbox transactionnel : une panne entre le commit et la publication peut encore perdre un evenement. Le retry Kafka ne remplace pas un outbox durable.
- SMTP ne garantit pas un envoi exactement une fois : une panne apres acceptation SMTP et avant enregistrement peut occasionner un doublon. La deduplication normale d'evenements repetes est couverte par les tests.
- La protection locale de demandes concurrentes est par instance, avec unicite en base. Un conflit entre plusieurs instances peut demander une nouvelle tentative du client ; la matrice de tests ne revendique pas une validation multi-noeuds.

## Appliquer a votre depot Git apres verification

Le fichier `CORRECTIONS.patch` correspond exactement au ZIP fourni au depart. Pour ne pas modifier les tags V1 a V6, ajouter les corrections dans une nouvelle branche :

```powershell
cd E:\Tele_Care-test
git switch -c corrections-validation
git apply --check E:\Tele_Care-corrige\CORRECTIONS.patch
git apply E:\Tele_Care-corrige\CORRECTIONS.patch
git add .
git commit -m "Corriger integration Tele-Care et ajouter verification automatique"
git push -u origin corrections-validation
```

Si `git apply --check` refuse, ne pas forcer : le depot a change depuis le ZIP. Les bases, sorties de build et dossiers IntelliJ sont ignores. Examiner `git status` avant le commit. La fusion vers main et la creation d'un nouveau tag de correction se font apres validation ; aucun ancien tag n'est deplace automatiquement.

## References techniques

- Compatibilite Spring Boot / Spring Cloud : https://spring.io/projects/spring-cloud/
- Envoi SMTP Spring Boot : https://docs.spring.io/spring-boot/reference/io/email.html
- Mailpit Docker : https://mailpit.axllent.org/docs/install/docker/
