# Ndaje App - Backend (Microservices)

## Présentation
Ndaje App est une plateforme de covoiturage moderne utilisant une architecture microservices sécurisée avec Keycloak et un stockage cloud Cloudflare R2.

## Architecture & Ports
| Service | Port | Description |
|---------|------|-------------|
| **Keycloak (Auth/Admin)** | 8081 | Serveur d'identité et gestion des rôles |
| **API Gateway** | 9000 | Point d'entrée unique (Routage centralisé) |
| **Eureka Server** | 8761 | Service Discovery |
| **User Service** | 8082 | Gestion des profils, inscriptions et documents utilisateurs (MinIO) |
| **Trip Service** | 8084 | Gestion des déplacements (Trajet/Caravane) et réservations |
| **Car Service** | 8089 | Gestion des véhicules et documents justificatifs (MinIO) |

## Installation & Démarrage

### 1. Prérequis
- Java 21 & Maven 3.8+
- Compte Cloudflare (R2)
- Keycloak installé et configuré (ndajee-realm)

### 2. Configuration
Créer un fichier `.env` à la racine :
- `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_ENDPOINT`, `MINIO_BUCKET` (Pour MinIO ou R2)
- Configuration DB (PostgreSQL) et JWT Keycloak.

### 3. Lancer tout le système
Utilisez le script batch fourni à la racine :
```powershell
.\run-all.bat
```

## Documentation des APIs (Via Gateway : 9000)

### User Service
- `POST /api/users/register/passenger` : Inscription passager
- `POST /api/users/register/driver` : Inscription conducteur
- `POST /api/users/login` : Connexion (Retourne JWT)
- `POST /api/users/forgot-password?email=...` : Reset password
- `PUT /api/users/{id}/profile` : Mise à jour profil
- `GET /api/admin/users` : (Admin) Liste tous les utilisateurs
- `DELETE /api/admin/users/{id}` : (Admin) Suppression

### User Document Service
- `POST /api/users/{userId}/documents` : Upload de fichier (Multipart)
- `GET /api/users/{userId}/documents/{documentId}` : Téléchargement du fichier
- `GET /api/users/{userId}/documents` : Liste par utilisateur

### Trip Service
- `GET /api/trips/hello` : Test de connectivité
- *Caravanes* : `POST /api/caravanes/`
- *Réservations* : `POST /api/reservations/`

### Car Service
- `POST /api/vehicules` : Créer un véhicule (Statut initial: `EN_ATTENTE`)
- `GET /api/vehicules` : Liste tous les véhicules
- `GET /api/vehicules/{id}` : Détails d'un véhicule
- `GET /api/vehicules/driver/{driverId}` : Liste par conducteur
- `POST /api/vehicules/{id}/documents` : Upload document justificatif (Carte Grise, Assurance)
- `GET /api/vehicules/{id}/documents` : Liste des documents d'un véhicule

---

## Guide de Test Postman

### 1. Obtenir un Token
- **URL** : `POST http://localhost:9000/api/users/login`
- **Body** (JSON) :
```json
{
  "email": "test@example.com",
  "password": "votre_password"
}
```
- Copiez le `token` dans la réponse.

### 2. Utiliser le Token
Dans Postman :
1. Allez dans l'onglet **Authorization**.
2. Type : **Bearer Token**.
3. Collez votre token.

### 3. Tester l'Upload (Via User ou Car Service)
- **URL** : `POST http://localhost:9000/api/users/{userId}/documents` (ou `/api/vehicules/{vehiculeId}/documents`)
- **Body** : `form-data`
  - `file` : (Type File) choisissez un fichier

---

## Technologies
- **Framework** : Spring Boot 3.2, Spring Cloud (Gateway, Eureka)
- **Sécurité** : Keycloak, OAuth2 Resource Server, JWT
- **Stockage** : MinIO (S3 Compatible)
- **BDD** : PostgreSQL (Local/Prod)
- **Communication** : REST, Maven

## User Stories

### Conducteur (DRIVER)
- **Créer un trajet** : En tant que conducteur, je veux publier un nouveau trajet avec un véhicule, un point de départ, une arrivée, une date et un prix.
  - `POST /api/trips`
- **Modifier un trajet** : En tant que conducteur, je veux mettre à jour les détails de mon trajet.
  - `PUT /api/trips/{id}`
- **Annuler un trajet** : (Non implémenté explicitement dans le contrôleur, mais `updateTripStatus` peut être utilisé)
- **Consulter la liste de ses trajets** : En tant que conducteur, je veux voir l'historique de mes trajets proposés.
  - `GET /api/trips/driver/{driverId}`

### Passager (PASSENGER)
- **Consulter les trajets disponibles** : En tant que passager, je veux voir les trajets disponibles.
  - `GET /api/trips`
- **Réserver un trajet** : En tant que passager, je veux réserver une ou plusieurs places sur un trajet.
  - `POST /api/reservations`
- **Consulter l'historique de ses réservations** : En tant que passager, je veux voir mes réservations passées et futures.
  - `GET /api/reservations/passenger/{passengerId}`

### Caravannier (CARAVANNIER)
- **Créer une caravane** : Organiser un voyage de groupe (pèlerinage, sortie).
  - `POST /api/caravanes`

## Microservices Endpoints

### User Service (Port 8082)
- `POST /api/users/register/passenger`
- `POST /api/users/register/driver`
- `GET /api/users/{id}`

### Trip Service (Port 8084)
- `POST /api/trips` (Create Trip)
- `POST /api/caravanes` (Create Caravane)
- `POST /api/reservations` (Create Reservation)
- `GET /api/trips/driver/{driverId}`
- `GET /api/reservations/passenger/{passengerId}`
- `GET /api/notations/trajet/{trajetId}`

### Car Service (Port 8089)
- `POST /api/vehicules` (Create Vehicle)
- `GET /api/vehicules/driver/{driverId}`
- `POST /api/vehicules/{id}/documents` (Upload Doc)
