@echo off
echo ========================================================
echo   Demarrage de Ndaje App Backend - Microservices
echo ========================================================

echo.
echo 1. Lancement de Eureka Server...
start "Eureka Server" cmd /k "mvn spring-boot:run -f eureka-server/pom.xml"

echo Attente de 15 secondes pour l'initialisation de Eureka...
timeout /t 15

echo.
echo 2. Lancement de Trip Service...
start "Trip Service" cmd /k "mvn spring-boot:run -f trip-service/pom.xml"

echo.
echo 3. Lancement de API Gateway...
start "API Gateway" cmd /k "mvn spring-boot:run -f api-gateway/pom.xml"

echo.
echo ========================================================
echo   Tous les services sont en cours de demarrage.
echo   Eureka Dashboard: http://localhost:8761
echo   Test Endpoint: http://localhost:8080/api/trips/hello
echo ========================================================
echo.
pause
