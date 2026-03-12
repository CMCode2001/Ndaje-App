# Rapport d'Audit et de Renforcement de la Sécurité - Ndajee App

## 1. Résumé Exécutif

Ce rapport documente les améliorations majeures apportées à l'architecture de sécurité de l'application Ndajee. L'objectif était de transformer une architecture de microservices standard en une plateforme résiliente capable de résister aux attaques modernes (XSS, Brute-force, BOLA, Fuites de données).

L'audit s'est concentré sur six piliers fondamentaux :
1.  **Intégrité des Sessions et Authentification**
2.  **Défense contre les Attaques Automatisées (Rate Limiting)**
3.  **Contrôle d'Accès au Niveau Objet (IDOR/BOLA)**
4.  **Protection de la Confidentialité (Chiffrement au Repos)**
5.  **Prévention des Fuites d'Informations (Logging/Erreurs)**
6.  **Sécurisation du Transport et des Headers**

---

## 2. Pilier 1 : Authentification et Intégrité des Sessions

### Problématique Initiale
Les jetons JWT (Access et Refresh) étaient stockés dans le `localStorage` du navigateur, les rendant accessibles à n'importe quel script malveillant via une faille XSS.

### Solutions implémentées
- **Cookies HttpOnly & Secure** : Les jetons sont désormais envoyés via des cookies `Set-Cookie` avec les flags `HttpOnly` (invisible au JavaScript), `Secure` (transmission HTTPS uniquement) et `SameSite=Strict` (prévention CSRF).
- **Architecture de Passerelle (API Gateway)** : Implémentation d'un filtre `CookieToBearerFilter` qui convertit automatiquement le cookie en header `Authorization: Bearer` pour les microservices cibles. Cela permet de sécuriser le frontend sans modifier la logique de validation JWT des microservices.
- **Frontend Stateless** : Le client React ne stocke plus aucun secret. Il utilise `credentials: 'include'` pour toutes ses requêtes, délégant la gestion des jetons au navigateur.

---

## 3. Pilier 2 : Défense de l'Infrastructure (Rate Limiting)

### Problématique Initiale
L'endpoint de connexion était exposé sans protection, permettant des attaques par force brute (brute-force) ou par dictionnaire à grande échelle.

### Solutions implémentées
- **Redis-backed Rate Limiter** : Utilisation de Redis au niveau de l'API Gateway pour suivre les requêtes par adresse IP.
- **Politique de Limitation** : Limitation stricte sur `POST /api/users/login` à un maximum de 5 requêtes par seconde avec une capacité de "burst" de 10. Toute tentative dépassant cette limite reçoit une erreur `429 Too Many Requests`.

---

## 4. Pilier 3 : Remédiation BOLA / IDOR (Broken Object Level Authorization)

### Problématique Initiale
Plusieurs endpoints permettaient d'accéder ou de modifier des ressources (trajets, véhicules, réservations) en changeant simplement l'identifiant dans l'URL, sans vérifier si l'utilisateur était le propriétaire.

### Défis relevés et Corrections
- **Validation du Propriétaire (Trips & Cars)** : Chaque action de modification (`PUT`, `DELETE`) vérifie désormais que le `driverId` de la ressource correspond à l'ID de l'utilisateur authentifié extrait du jeton.
- **Suppression des Paramètres Insecure (Reservations)** : L'endpoint de suppression de réservation acceptait un `passengerId` en paramètre de requête. Ce paramètre a été supprimé pour forcer l'utilisation de l'ID extrait de façon sécurisée depuis le `SecurityContext`.
- **Centralisation de la Vérification** : Utilisation de `SecurityUtils` pour garantir une logique d'appartenance uniforme sur tout le `user-service`.

---

## 5. Pilier 4 : Protection de la Confidentialité (Chiffrement au Repos)

### Problématique Initiale
Les données sensibles (`PII - Personally Identifiable Information`) étaient stockées en clair dans la base de données PostgreSQL/H2.

### Solutions implémentées
- **JPA AttributeConverters** : Création d'un convertisseur transparent utilisant l'algorithme de chiffrement symétrique **AES-128**.
- **Champs Protégés** :
    - **Utilisateurs** : Email et Numéro de téléphone.
    - **Conducteurs** : Numéro de permis.
    - **Véhicules** : Plaque d'immatriculation.
- **Avantage** : En cas de compromission physique ou de dump de la base de données, les données personnelles restent illisibles sans la clé cryptographique.

---

## 6. Pilier 5 : Prévention des Fuites d'Informations

### Problématique Initiale
Les erreurs systèmes (stacktraces, noms de colonnes SQL) étaient renvoyées au client, et les mots de passe risquaient d'apparaître dans les logs en cas d'affichage (`toString()`) des objets DTO.

### Solutions implémentées
- **Gestion Globale des Exceptions** : Les `GlobalExceptionHandler` de tous les services ont été bridés pour renvoyer des messages d'erreur opaques (ex: "Une erreur interne est survenue") au client, tout en conservant les logs détaillés côté serveur.
- **Audit des DTOs** : Utilisation de `@ToString.Exclude` (Lombok) sur les champs `password` dans `UserRegistrationRequest` et `LoginRequest`.

---

## 7. Pilier 6 : Headers de Sécurité et Configuration

### Corrections apportées
- **X-Frame-Options** : Réactivation de la protection contre le clickjacking via `frameOptions().sameOrigin()`.
- **Secrets Management** : Migration des secrets (`DB_PASSWORD`, `MINIO_SECRET_KEY`, etc.) des fichiers `application.yml` vers des variables d'environnement (`.env`), évitant leur commit accidentel dans le dépôt Git.

---

## 8. Conclusion et Recommandations Futures

L'application Ndajee dispose désormais d'une base de sécurité solide. Pour l'avenir, nous recommandons :
- **Rotation des clés** : Mettre en place un mécanisme de rotation pour la clé de chiffrement AES.
- **Audits de dépendances** : Utiliser des outils comme `Snyk` ou `OWASP Dependency-Check` de façon automatisée dans la CI/CD.
- **Surveillance Redis** : Monitorer les alertes du Rate Limiter pour identifier les tentatives d'attaques en temps réel.

---
*Ce rapport a été généré suite à la phase de durcissement sécuritaire du [2026-03-12].*
