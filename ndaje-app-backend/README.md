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
| **Trip Service** | 8084 | Gestion des trajets et réservations |

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
- `GET /api/trips/hello` : Test de connectivité

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
