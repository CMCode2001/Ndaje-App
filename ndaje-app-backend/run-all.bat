@echo off
setlocal enabledelayedexpansion
title Ndaje App Backend - Control Panel

:: Load secrets from .env file
if exist .env (
    for /f "usebackq tokens=1,* delims==" %%a in (".env") do (
        set "%%a=%%b"
    )
)

:menu
cls
echo ========================================================
echo        Ndaje App Backend - Panneau de controle
echo ========================================================
echo   Gestion des microservices Spring Boot
echo ========================================================
echo.
echo   1 - Demarrer tous les services
echo   2 - Arreter tous les services
echo   3 - Redemarrer tous les services
echo   4 - Quitter
echo.
echo ========================================================
set /p choice=Selectionnez une option (1-4) : 

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto stop_all
if "%choice%"=="3" goto restart_all
if "%choice%"=="4" goto exit_app

echo.
echo Choix invalide. Veuillez reessayer...
timeout /t 2 >nul
goto menu

:start_all
cls
echo Demarrage des microservices...
echo.

echo 1. Lancement de Eureka Server (Port 8761)...
start "Eureka Server" cmd /k "mvn spring-boot:run -f eureka-server/pom.xml"

echo Attente de 15 secondes pour l'initialisation de Eureka...
timeout /t 15 >nul

echo 2. Lancement de API Gateway (Port 9000)...
start "API Gateway" cmd /k "mvn spring-boot:run -f api-gateway/pom.xml"

echo 3. Lancement de User Service (Port 8082)...
start "User Service" cmd /k "mvn spring-boot:run -f user-service/pom.xml"

echo 4. Lancement de Trip Service (Port 8084)...
start "Trip Service" cmd /k "mvn spring-boot:run -f trip-service/pom.xml"

echo 5. Lancement de Document Service (Port 8086)...
start "Document Service" cmd /k "mvn spring-boot:run -f document-service/pom.xml"

echo 6. Lancement de Reservation Service (Port 8088)...
start "Reservation Service" cmd /k "mvn spring-boot:run -f reservation-service/pom.xml"

echo 7. Lancement de Car Service (Port 8089)...
start "Car Service" cmd /k "mvn spring-boot:run -f car-service/pom.xml"


echo.
echo ========================================================
echo   Tous les services sont en cours de demarrage
echo   Eureka Dashboard : http://localhost:8761
echo   API Gateway      : http://localhost:9000
echo   Test Endpoint    : http://localhost:9000/api/trips/hello
echo ========================================================
pause
goto menu

:stop_all
cls
echo Arret des services...
echo.

taskkill /FI "WINDOWTITLE eq Eureka Server*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq API Gateway*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq User Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Trip Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Document Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Reservation Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Car Service*" /T /F >nul 2>&1

echo Tous les services ont ete arretes.
pause
goto menu

:restart_all
cls
echo Redemarrage des services...
call :stop_all
timeout /t 3 >nul
call :start_all
goto menu

:exit_app
echo.
echo Fermeture du panneau de controle...
timeout /t 2 >nul
exit
