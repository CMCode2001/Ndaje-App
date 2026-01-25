# Ndaje App - Backend (Microservices)

## Présentation
Ndaje App est une plateforme de covoiturage moderne utilisant une architecture microservices sécurisée avec Keycloak et un stockage cloud Cloudflare R2.

## Architecture & Ports
| Service | Port | Description |
|---------|------|-------------|
| **Keycloak (Auth/Admin)** | 8081 | Serveur d'identité et gestion des rôles |
| **API Gateway** | 9000 | Point d'entrée unique (Routage centralisé) |
| **Eureka Server** | 8761 | Service Discovery |
| **User Service** | 8082 | Gestion des profils, inscriptions et admin |
| **Document Service** | 8086 | Gestion des documents (Kyc, Permis) via R2 |
| **Trip Service** | 8084 | Gestion des trajets |
| **Reservation Service** | 8088 | Gestion des réservations |
| **Car Service** | 8083 | Gestion des véhicules |

## Installation & Démarrage

### 1. Prérequis
- Java 21 & Maven 3.8+
- Compte Cloudflare (R2)
- Keycloak installé et configuré (ndajee-realm)

### 2. Configuration
Créez les variables d'environnement suivantes ou modifiez les fichiers `application.yml` :
- `R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`, `R2_ACCOUNT_ID` (Pour le stockage)

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

### Document Service
- `POST /api/documents` : Upload de fichier (Multipart)
- `GET /api/documents/{id}` : Téléchargement du fichier
- `GET /api/documents/{id}/metadata` : Détails du document
- `GET /api/documents?utilisateurId=...` : Liste par utilisateur

### Trip Service
- `GET /api/trips` : Liste tous les trajets
- `POST /api/trips` : Créer un trajet
- `GET /api/trips/{id}` : Détails d'un trajet
- `PUT /api/trips/{id}` : Modifier un trajet
- `GET /api/trips/driver/{driverId}` : Trajets par conducteur
- `POST /api/trips/{id}/decrement-seats` : Réserver des places (Interne)
- `POST /api/trips/{id}/increment-seats` : Libérer des places (Interne)

### Reservation Service
- `POST /api/reservations` : Créer une réservation
- `GET /api/reservations/passenger/{passengerId}` : Historique passager
- `PUT /api/reservations/{id}` : Modifier une réservation (places)
- `PATCH /api/reservations/{id}/cancel` : Annuler une réservation

### Car Service
- `POST /api/vehicules` : Ajouter un véhicule
- `GET /api/vehicules/driver/{driverId}` : Véhicules par conducteur
- `PUT /api/vehicules/{id}` : Modifier un véhicule
- `DELETE /api/vehicules/{id}` : Supprimer un véhicule

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

### 3. Tester l'Upload (Document Service)
- **URL** : `POST http://localhost:9000/api/documents`
- **Body** : `form-data`
  - `file` : (Type File) choisissez un fichier
  - `utilisateurId` : l'ID Keycloak de l'utilisateur

---

## Technologies
- **Framework** : Spring Boot 3.2, Spring Cloud (Gateway, Eureka)
- **Sécurité** : Keycloak, OAuth2 Resource Server, JWT
- **Stockage** : Cloudflare R2 (API compatible S3)
- **BDD** : H2 (Dev) / PostgreSQL (Prod)
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

## Microservices Endpoints

### User Service (Port 8082)
- `POST /api/users/register/passenger`
- `POST /api/users/register/driver`
- `GET /api/users/{id}`

### Trip Service (Port 8084)
- `POST /api/trips` (Create Trip)
- `GET /api/trips` (List All)
- `GET /api/trips/{id}` (Get One)
- `GET /api/trips/driver/{driverId}` (List by Driver)
- `PUT /api/trips/{id}` (Update Trip)
- `POST /api/trips/{id}/decrement-seats` (Internal)
- `POST /api/trips/{id}/increment-seats` (Internal)

### Reservation Service (Port 8088)
- `POST /api/reservations` (Create)
- `GET /api/reservations/passenger/{passengerId}` (History)
- `PUT /api/reservations/{id}` (Update)
- `PATCH /api/reservations/{id}/cancel` (Cancel)

### Car Service (Port 8083)
- `POST /api/vehicules` (Create)
- `GET /api/vehicules/driver/{driverId}` (List by Driver)
- `PUT /api/vehicules/{id}` (Update)
- `DELETE /api/vehicules/{id}` (Delete)

---

## Consommation Frontend

### 1. Authentification
Toutes les requêtes (sauf login/register) nécessitent un Header `Authorization`.

**Exemple Axios :**
```javascript
const api = axios.create({
  baseURL: 'http://localhost:9000/api'
});

// Joindre le token à chaque requête
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});
```

### 2. Récupération des Trajets (`/api/trips`)
La réponse inclut désormais les informations du conducteur pour éviter des appels supplémentaires.

**Structure de l'objet `data` :**
```json
{
  "id": 1,
  "driverId": "uuid-keycloak",
  "driverFirstName": "Jean",
  "driverLastName": "Dupont",
  "driverPhone": "+22177...",
  "depart": "Dakar",
  "arrivee": "Saint-Louis",
  "dateDepart": "2026-01-25T10:00:00",
  "placesDisponibles": 3,
  "prix": 5000.0,
  "statutTrajet": "CREATED"
}
```

### 3. Publication de Trajet (`POST /api/trips`)
Nécessite le rôle `DRIVER`. L'ID du véhicule doit être un ID existant appartenant au conducteur.

**Exemple de Body (JSON) :**
```json
{
  "depart": "Guediawaye",
  "arrivee": "Plateau",
  "dateDepart": "2026-02-01T08:00:00",
  "placesDisponibles": 4,
  "prix": 1500,
  "vehicleId": "1"
}
```
*Note : `driverId` est extrait automatiquement du token JWT si non fourni.*

### 4. Historique des Réservations (`/api/reservations/passenger/{id}`)
La réponse inclut les détails du trajet réservé.

**Structure de l'objet `data` (Liste) :**
```json
[
  {
    "id": 10,
    "tripId": 1,
    "depart": "Dakar",
    "arrivee": "Saint-Louis",
    "dateDepart": "2026-01-25T10:00:00",
    "reservationDate": "2026-01-24T18:00:00",
    "places": 2,
    "status": "CONFIRMED"
  }
]
```

### 4. Gestion des Erreurs
Le backend renvoie des messages d'erreur explicites en cas d'échec (ex: dépassement de capacité).
```json
{
  "success": false,
  "message": "Available seats (6) cannot exceed vehicle capacity (4)",
  "data": null
}
```
