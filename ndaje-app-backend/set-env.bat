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

REM MinIO Configuration
set MINIO_ACCESS_KEY=minioadmin
set MINIO_SECRET_KEY=minioadmin
set MINIO_ENDPOINT=http://localhost:9002
set MINIO_BUCKET=ndajee-bucket-document

echo Environment variables loaded successfully!
