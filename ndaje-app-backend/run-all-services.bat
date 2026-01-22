@echo off
setlocal enabledelayedexpansion
title Ndaje App Controller

:main_menu
cls
echo ========================================================
echo        Ndaje App Backend - Panneau de controle
echo ========================================================
echo   Gestion des Microservices (Sans Keycloak - Manuel)
echo ========================================================
echo.
echo 1. TOUT DEMARRER (Eureka + Services)
echo 2. TOUT ARRETER
echo 3. TOUT REDEMARRER
echo 4. Gestion INDIVIDUELLE des services
echo 5. Quitter
echo.
set /p choice="Selectionnez une option (1-5) : "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto stop_all
if "%choice%"=="3" goto restart_all
if "%choice%"=="4" goto individual_menu
if "%choice%"=="5" goto exit
goto main_menu

:individual_menu
cls
echo ========================================================
echo        Gestion Individuelle
echo ========================================================
echo.
echo 1. Eureka Server
echo 2. API Gateway
echo 3. User Service
echo 4. Trip Service
echo 5. Reservation Service
echo 6. Document Service
echo 7. Retour au menu principal
echo.
set /p srv_choice="Selectionnez un service a demarrer (1-7) : "

if "%srv_choice%"=="1" goto start_eureka
if "%srv_choice%"=="2" goto start_gateway
if "%srv_choice%"=="3" goto start_user
if "%srv_choice%"=="4" goto start_trip
if "%srv_choice%"=="5" goto start_reservation
if "%srv_choice%"=="6" goto start_document
if "%srv_choice%"=="7" goto main_menu
goto individual_menu

:: ==========================================
:: ACTIONS GLOBALES
:: ==========================================

:start_all
echo.
echo --------------------------------------------------------
echo 1. Lancement de Eureka Server...
echo --------------------------------------------------------
start "Ndaje-Eureka" cmd /k "mvn spring-boot:run -f eureka-server/pom.xml"

echo.
echo Attente de 15 secondes pour l'initialisation de Eureka...
timeout /t 15 /nobreak

echo.
echo --------------------------------------------------------
echo 2. Lancement des Services Metier...
echo --------------------------------------------------------
echo - User Service...
start "Ndaje-User" cmd /k "mvn spring-boot:run -f user-service/pom.xml"
timeout /t 5 /nobreak

echo - Trip Service...
start "Ndaje-Trip" cmd /k "mvn spring-boot:run -f trip-service/pom.xml"

echo - Reservation Service...
start "Ndaje-Reservation" cmd /k "mvn spring-boot:run -f reservation-service/pom.xml"

echo - Document Service...
start "Ndaje-Document" cmd /k "mvn spring-boot:run -f document-service/pom.xml"

echo.
echo --------------------------------------------------------
echo 3. Lancement de API Gateway...
echo --------------------------------------------------------
timeout /t 10 /nobreak
start "Ndaje-Gateway" cmd /k "mvn spring-boot:run -f api-gateway/pom.xml"

echo.
echo ========================================================
echo   Tous les services ont ete lances !
echo   Eureka: http://localhost:8761
echo   Assurez-vous que Keycloak est lance manuellement.
echo ========================================================
pause
goto main_menu

:stop_all
echo.
echo Arret des services en cours...
taskkill /FI "WINDOWTITLE eq Ndaje-*" /T /F
echo.
echo Tous les services geres par ce script ont ete arretes.
pause
goto main_menu

:restart_all
call :stop_all
timeout /t 5
goto start_all

:: ==========================================
:: DEMARRAGE INDIVIDUEL
:: ==========================================

:start_eureka
start "Ndaje-Eureka" cmd /k "mvn spring-boot:run -f eureka-server/pom.xml"
goto individual_menu

:start_gateway
start "Ndaje-Gateway" cmd /k "mvn spring-boot:run -f api-gateway/pom.xml"
goto individual_menu

:start_user
start "Ndaje-User" cmd /k "mvn spring-boot:run -f user-service/pom.xml"
goto individual_menu

:start_trip
start "Ndaje-Trip" cmd /k "mvn spring-boot:run -f trip-service/pom.xml"
goto individual_menu

:start_reservation
start "Ndaje-Reservation" cmd /k "mvn spring-boot:run -f reservation-service/pom.xml"
goto individual_menu

:start_document
start "Ndaje-Document" cmd /k "mvn spring-boot:run -f document-service/pom.xml"
goto individual_menu

:exit
exit
