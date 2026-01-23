@echo off
REM Load environment variables from .env file

REM Keycloak Configuration
set KEYCLOAK_SERVER_URL=http://localhost:8081
set KEYCLOAK_REALM=ndajee-realm
set KEYCLOAK_ADMIN_CLIENT_ID=ndajee-client
set KEYCLOAK_ADMIN_CLIENT_SECRET=juYkaotpPqXyjpXC0NFttPMS7hYsiOPL

REM Database Configuration
set DB_USERNAME=postgres
set DB_PASSWORD=admin

REM Database URLs for each service
set USER_DB_URL=jdbc:postgresql://localhost:5432/ndaje-user-db
set TRIP_DB_URL=jdbc:postgresql://localhost:5432/ndaje-trip-db
set RESERVATION_DB_URL=jdbc:postgresql://localhost:5432/ndaje-reservation-db
set Document_DB_URL=jdbc:postgresql://localhost:5432/ndaje-document-db
set Car_DB_URL=jdbc:postgresql://localhost:5432/ndaje-car-db

REM Cloudflare R2 Configuration
set R2_ACCOUNT_ID=eb29816742395623567cbace0d59f0d0
set R2_ACCESS_KEY=2c2b6024a91313a4670adc0b7237bf7b
set R2_SECRET_KEY=e6e2385faddab303a19a22708447ae0c2d179948ebbdabaad0a96b15e42228ad
set R2_BUCKET=ndajee-documents
set R2_ENDPOINT=https://eb29816742395623567cbace0d59f0d0.r2.cloudflarestorage.com

echo Environment variables loaded successfully!
