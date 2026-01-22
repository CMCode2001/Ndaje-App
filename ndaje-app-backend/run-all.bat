@echo off
setlocal enabledelayedexpansion

:menu
cls
echo ========================================================
echo        Ndaje App Backend - Panneau de controle
echo ========================================================
echo.
echo  [1] Demarrer tous les microservices
echo  [2] Arreter tous les microservices
echo  [3] Redemarrer tous les microservices
echo  [4] Quitter
echo.
echo  Note: Assurez-vous que Keycloak est demarre sur le port 8081.
echo ========================================================
set /p choice="Selectionnez une option (1-4) : "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto stop_all
if "%choice%"=="3" goto restart_all
if "%choice%"=="4" goto exit
goto menu

:start_all
echo.
echo [1/5] Lancement de Eureka Server...
start "Eureka Server" cmd /k "mvn spring-boot:run -f eureka-server/pom.xml"
timeout /t 10

echo [2/5] Lancement de User Service (Port 8082)...
start "User Service" cmd /k "mvn spring-boot:run -f user-service/pom.xml"

echo [3/5] Lancement de Document Service (Port 8086)...
start "Document Service" cmd /k "mvn spring-boot:run -f document-service/pom.xml"

echo [4/5] Lancement de Trip Service (Port 8084)...
start "Trip Service" cmd /k "mvn spring-boot:run -f trip-service/pom.xml"

echo [5/5] Lancement de API Gateway (Port 9000)...
start "API Gateway" cmd /k "mvn spring-boot:run -f api-gateway/pom.xml"

echo.
echo Tous les services sont en cours de demarrage.
echo Eureka  : http://localhost:8761
echo Gateway : http://localhost:9000
pause
goto menu

:stop_all
echo.
echo Arret des services en cours...
taskkill /FI "WINDOWTITLE eq Eureka Server*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq User Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Document Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Trip Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq API Gateway*" /T /F >nul 2>&1
echo.
echo Tous les services ont ete arretes.
pause
goto menu

:restart_all
echo Redemarrage en cours...
taskkill /FI "WINDOWTITLE eq Eureka Server*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq User Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Document Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq Trip Service*" /T /F >nul 2>&1
taskkill /FI "WINDOWTITLE eq API Gateway*" /T /F >nul 2>&1
timeout /t 2
goto start_all

:exit
echo Au revoir !
exit
