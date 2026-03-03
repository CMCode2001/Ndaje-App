# Document Service

Microservice Spring Boot pour la gestion de documents utilisateurs avec stockage **MinIO**.

## Fonctionnalités

- ✅ Upload de documents vers MinIO
- ✅ Téléchargement de documents depuis MinIO
- ✅ Suppression de documents (R2 + métadonnées)
- ✅ Liste des documents (tous ou par utilisateur)
- ✅ Gestion des métadonnées en base de données (H2/PostgreSQL)
- ✅ Validation et gestion des exceptions
- ✅ Logging complet

## Architecture

```
document-service/
├── config/          # Configuration R2 (S3-compatible)
├── controller/      # REST endpoints
├── dto/             # Data Transfer Objects
├── entity/          # Entités JPA
├── exception/       # Exceptions personnalisées
├── repository/      # Repositories JPA
└── service/         # Logique métier
```

## Prérequis

- Java 21
- Maven 3.8+
- Service MinIO installé et accessible
- Bucket MinIO créé

## Configuration MinIO

### 1. Créer un bucket MinIO

1. Connectez-vous à la console d'administration MinIO
2. Allez dans **Buckets** dans le menu latéral
3. Cliquez sur **Create bucket**
4. Nommez votre bucket: `ndajee-documents`

### 2. Créer des Access Keys MinIO

1. Dans la console MinIO, allez dans **Access Keys**
2. Cliquez sur **Create Access Key**
3. Notez:
   - **Access Key**
   - **Secret Key**

### 3. Configurer les variables d'environnement

```bash
# Windows (PowerShell)
$env:MINIO_BUCKET="ndajee-documents"
$env:MINIO_ACCESS_KEY="votre-access-key"
$env:MINIO_SECRET_KEY="votre-secret-key"
$env:MINIO_ENDPOINT="http://localhost:9000"

# Linux/Mac
export MINIO_BUCKET=ndajee-documents
export MINIO_ACCESS_KEY=votre-access-key
export MINIO_SECRET_KEY=votre-secret-key
export MINIO_ENDPOINT=http://localhost:9000
```

Ou modifiez directement `application.yml`:

```yaml
minio:
  bucket: ndajee-documents
  endpoint: http://localhost:9000
  access-key: votre-access-key
  secret-key: votre-secret-key
```

## Démarrage

```bash
# Compiler
mvn clean compile

# Lancer l'application
mvn spring-boot:run
```

L'application démarre sur `http://localhost:8083`

## Endpoints API

### 1. Upload un document

```bash
POST /api/documents?utilisateurId=user123
Content-Type: multipart/form-data

curl -X POST "http://localhost:8083/api/documents?utilisateurId=user123" \
  -F "file=@/path/to/document.pdf"
```

**Réponse:**
```json
{
  "id": 1,
  "nom": "document.pdf",
  "type": "application/pdf",
  "taille": 102400,
  "dateUpload": "2026-01-17T14:30:00",
  "utilisateurId": "user123",
  "urlS3": "user123/uuid-123.pdf"
}
```

### 2. Télécharger un document

```bash
GET /api/documents/{id}

curl -O -J "http://localhost:8083/api/documents/1"
```

### 3. Obtenir les métadonnées

```bash
GET /api/documents/{id}/metadata

curl "http://localhost:8083/api/documents/1/metadata"
```

### 4. Lister les documents

```bash
# Tous les documents
GET /api/documents

# Documents d'un utilisateur
GET /api/documents?utilisateurId=user123

curl "http://localhost:8083/api/documents?utilisateurId=user123"
```

### 5. Supprimer un document

```bash
DELETE /api/documents/{id}

curl -X DELETE "http://localhost:8083/api/documents/1"
```

## Limites

- Taille maximale de fichier: **10 MB**
- Types de fichiers: Tous (validation côté client recommandée)

## Base de données

### H2 (Développement)

Console H2: http://localhost:8083/h2-console

- JDBC URL: `jdbc:h2:mem:documentdb`
- Username: `sa`
- Password: *(vide)*

### PostgreSQL (Production)

Modifiez `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/documentdb
    username: postgres
    password: votre-password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

## Sécurité

⚠️ **Important**: Ce service n'implémente pas encore d'authentification JWT. Pour la production:

1. Ajoutez Spring Security + OAuth2
2. Validez le `utilisateurId` depuis le token JWT
3. Implémentez des contrôles d'accès (un utilisateur ne peut accéder qu'à ses documents)

## Logs

Les logs sont configurés dans `application.yml`:

```yaml
logging:
  level:
    com.ndajee.documentservice: DEBUG
    software.amazon.awssdk: INFO
```

## Dépannage

### Erreur: "Unable to load credentials from system settings"

→ Vérifiez que les variables d'environnement MinIO sont définies (`MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`)

### Erreur: "The specified bucket does not exist"

→ Créez le bucket MinIO dans la console d'administration ou vérifiez le nom dans `application.yml`

### Erreur: "Access Denied" ou "403 Forbidden"

→ Vérifiez vos Access Keys MinIO.

### Erreur: "Connection refused"

→ Vérifiez que le service MinIO est bien en cours de démarrage ou que le endpoint fourni dans `application.yml` (`MINIO_ENDPOINT`) est correct.

## Avantages de MinIO

- ✅ **Self-hosted** : Contrôle total sur les données
- ✅ **Haute performance** : Conçu pour être extrêmement rapide
- ✅ **Compatible S3** : Utilise la même API qu'AWS S3
- ✅ **Facile à déployer** : Déploiement via conteneur Docker ou binaire unique

## Technologies

- Spring Boot 3.2.1
- AWS SDK for Java 2.21.0
- H2 Database / PostgreSQL
- Lombok
- Jakarta Validation

## Licence

Propriétaire - Ndajee © 2026
