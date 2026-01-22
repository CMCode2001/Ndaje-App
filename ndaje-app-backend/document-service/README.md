# Document Service

Microservice Spring Boot pour la gestion de documents utilisateurs avec stockage **Cloudflare R2**.

## Fonctionnalités

- ✅ Upload de documents vers Cloudflare R2
- ✅ Téléchargement de documents depuis R2
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
- Compte Cloudflare avec R2 activé
- Bucket R2 créé

## Configuration Cloudflare R2

### 1. Créer un bucket R2

1. Connectez-vous à [Cloudflare Dashboard](https://dash.cloudflare.com/)
2. Allez dans **R2** dans le menu latéral
3. Cliquez sur **Create bucket**
4. Nommez votre bucket: `ndajee-documents`
5. Choisissez la région (ou laissez "Automatic")

### 2. Créer des API Tokens R2

1. Dans R2, allez dans **Manage R2 API Tokens**
2. Cliquez sur **Create API Token**
3. Donnez un nom: `ndajee-document-service`
4. Permissions: **Object Read & Write**
5. Notez:
   - **Access Key ID**
   - **Secret Access Key**
   - **Account ID** (visible dans l'URL du dashboard)

### 3. Configurer les variables d'environnement

```bash
# Windows (PowerShell)
$env:R2_BUCKET_NAME="ndajee-documents"
$env:R2_ACCOUNT_ID="votre-account-id"
$env:R2_ACCESS_KEY_ID="votre-access-key"
$env:R2_SECRET_ACCESS_KEY="votre-secret-key"

# Linux/Mac
export R2_BUCKET_NAME=ndajee-documents
export R2_ACCOUNT_ID=votre-account-id
export R2_ACCESS_KEY_ID=votre-access-key
export R2_SECRET_ACCESS_KEY=votre-secret-key
```

Ou modifiez directement `application.yml`:

```yaml
cloudflare:
  r2:
    bucket-name: ndajee-documents
    account-id: votre-account-id
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

→ Vérifiez que les variables d'environnement R2 sont définies (`R2_ACCESS_KEY_ID`, `R2_SECRET_ACCESS_KEY`)

### Erreur: "The specified bucket does not exist"

→ Créez le bucket R2 dans le dashboard Cloudflare ou vérifiez le nom dans `application.yml`

### Erreur: "Access Denied" ou "403 Forbidden"

→ Vérifiez que votre API Token R2 a les permissions **Object Read & Write**

### Erreur: "Invalid endpoint"

→ Vérifiez que `R2_ACCOUNT_ID` est correctement défini (trouvable dans l'URL du dashboard Cloudflare)

## Avantages de Cloudflare R2

- ✅ **Gratuit** : 10 GB de stockage gratuit par mois
- ✅ **Pas de frais de sortie** : Transferts de données gratuits (contrairement à AWS S3)
- ✅ **Compatible S3** : Utilise la même API qu'AWS S3
- ✅ **Rapide** : Réseau global Cloudflare
- ✅ **Simple** : Pas de configuration de régions complexes

## Technologies

- Spring Boot 3.2.1
- AWS SDK for Java 2.21.0
- H2 Database / PostgreSQL
- Lombok
- Jakarta Validation

## Licence

Propriétaire - Ndajee © 2026
