@echo off
setlocal enabledelayedexpansion

:menu
cls
echo ========================================================
echo        Ndaje App Backend - Panneau de controle
echo ========================================================
echo   Demarrage de Ndaje App Backend - Microservices
echo ========================================================
set /p choice="Selectionnez une option (1-4) : "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto stop_all
if "%choice%"=="3" goto restart_all
if "%choice%"=="4" goto exit
goto menu

:start_all
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
