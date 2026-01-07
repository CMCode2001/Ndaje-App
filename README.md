# Ndaje App - Backend Microservices

Bienvenue sur le repository backend de **Ndaje App**, une application de covoiturage moderne et scalable.

Ce projet adopte une architecture microservices basée sur l'écosystème **Spring Boot** et **Spring Cloud**.

## 🏗 Architecture

Le système est composé des microservices suivants :

| Service | Description | Port par défaut |
|---------|-------------|-----------------|
| **eureka-server** | Serveur de découverte de services (Service Registry). Permet aux services de se trouver dynamiquement. | `8761` |
| **api-gateway** | Point d'entrée unique (Edge Server). Gère le routage, la sécurité et l'équilibrage de charge. | `8080` |
| **trip-service** | Service métier gérant les trajets et les réservations de covoiturage. | `8081` |

### Flux de Communication
`Client` -> `API Gateway (8080)` -> `Service Registry (Eureka)` -> `Microservice Cible (ex: Trip Service)`

## 🚀 Pré-requis

- **Java 17** ou supérieur
- **Maven 3.8** ou supérieur

## 🛠 Installation et Démarrage

### 1. Clonage et Build
À la racine du projet `ndaje-app-backend` :

```bash
mvn clean install
```

### 2. Démarrage Automatique (Windows)
Un script de démarrage est disponible à la racine du projet pour lancer tous les services dans le bon ordre :

Double-cliquez sur `run-all.bat`
*(Ouvrira des terminaux séparés pour chaque service)*

### 3. Démarrage Manuel
Si vous préférez lancer les services manuellement, respectez l'ordre suivant :

1. **Eureka Server**
   ```bash
   cd ndaje-app-backend/eureka-server
   mvn spring-boot:run
   ```
   *Attendre le démarrage complet.*

2. **Trip Service** (et autres microservices métier)
   ```bash
   cd ndaje-app-backend/trip-service
   mvn spring-boot:run
   ```

3. **API Gateway**
   ```bash
   cd ndaje-app-backend/api-gateway
   mvn spring-boot:run
   ```

## 🔍 Vérification et Tests

- **Tableau de bord Eureka** : [http://localhost:8761](http://localhost:8761)
  - Vérifiez que `TRIP-SERVICE` et `API-GATEWAY` sont bien enregistrés.

- **Test API (via Gateway)** :
  - Endpoint : `GET http://localhost:8080/api/trips/hello`
  - Réponse attendue : `Hello from Trip Service!`

## 📂 Structure du projet

```
ndaje-app-backend/
├── pom.xml                 # POM Parent (Spring Boot + Spring Cloud management)
├── eureka-server/          # Service Discovery
├── api-gateway/            # Routing & Filtering
└── trip-service/           # Logic métier (Trajets)
```

## 🔮 Évolutions Futures
- Ajout d'un `auth-service` (Oauth2/JWT)
- Ajout d'un `reservation-service`
- Base de données PostgreSQL par service
- Centralized Configuration (Spring Cloud Config)
