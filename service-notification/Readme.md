# Service Notifications

Port : 8085. Consomme les topics Kafka `intervention-assigned` et `emergency-resolved`, recupere les contacts de la personne via Feign, puis envoie un vrai mail SMTP.

Le beneficiaire doit avoir au moins un contact avec une adresse email valide. Sans email/contact, le service ne fabrique pas de destinataire. Le message est enregistre comme envoye apres acceptation par le serveur SMTP. Les erreurs SMTP remontent au consumer pour permettre ses reessais.

`eventId` est unique en base pour dedupliquer les evenements repetes. Les notifications exposees incluent le destinataire email et l'identifiant de l'evenement.

- GET `/notifications`
- GET `/notifications?alertId={id}`
- GET `/notifications/{id}`

En demonstration, SMTP est fourni par Mailpit sur 1025 et les mails recus sont visibles sur http://localhost:8025. Cela teste un vrai envoi SMTP local, sans livrer de message a une boite Gmail/Outlook externe.

Voir [le guide commun](../LISEZMOI_CORRECTIONS.md) pour le lancement, les tests, la configuration SMTP externe et les limites de garantie d'envoi.
