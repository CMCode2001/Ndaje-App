@echo off
setlocal
title Ndaje App Launcher

:MENU
cls
color 0B
echo ========================================================
echo   NDAJE APP BACKEND - LAUNCHER
echo ========================================================
echo.
echo   1. Construire et Demarrer tout (Build & Run)
echo   2. Demarrer tout sans construire (Run only)
echo   3. Construire seulement (Build only)
echo   4. Arreter tous les processus Java (Kill Java)
echo   5. Quitter
echo.
set /p choice=Choisissez une option (1-5): 

if "%choice%"=="1" goto BUILD_AND_RUN
if "%choice%"=="2" goto RUN_ALL
if "%choice%"=="3" goto BUILD_ONLY
if "%choice%"=="4" goto KILL_JAVA
if "%choice%"=="5" goto EXIT
goto MENU

:BUILD_ONLY
cls
call :BUILD_PROJECTS
echo.
echo Construction terminee.
pause
goto MENU

:BUILD_AND_RUN
cls
call :BUILD_PROJECTS
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERREUR: La construction a echoue. Arret du demarrage.
    pause
    goto MENU
)
goto RUN_ALL

:RUN_ALL
cls
echo ========================================================
echo   DEMARRAGE DES MICROSERVICES
echo ========================================================

echo.
echo [1/3] Demarrage de EUREKA SERVER...
start "Eureka Server" cmd /k "title Eureka Server && mvn spring-boot:run -f eureka-server/pom.xml"

echo.
echo Attente de 10 secondes pour l'initialisation de Eureka...
timeout /t 10 /nobreak >nul

echo.
echo [2/3] Demarrage de API GATEWAY...
start "API Gateway" cmd /k "title API Gateway && mvn spring-boot:run -f api-gateway/pom.xml"

echo.
echo [3/3] Demarrage de TRIP SERVICE...
start "Trip Service" cmd /k "title Trip Service && mvn spring-boot:run -f trip-service/pom.xml"

echo.
echo [4/4] Demarrage de RESERVATION SERVICE...
start "Reservation Service" cmd /k "title Reservation Service && mvn spring-boot:run -f reservation-service/pom.xml"

echo.
echo ========================================================
echo   TOUS LES SERVICES SONT LANCES
echo ========================================================
echo.
echo   Eureka Dashboard : http://localhost:8761
echo   Trip Service     : http://localhost:8091
echo   API Gateway      : http://localhost:8080
echo   Reservation Svc  : http://localhost:8096
echo.
echo   Appuyez sur une touche pour revenir au menu...
pause >nul
goto MENU

:KILL_JAVA
cls
echo ========================================================
echo   ARRET FORCE DES PROCESSUS JAVA
echo ========================================================
echo.
taskkill /f /im java.exe
echo.
echo Processus nettoyes.
pause
goto MENU

:BUILD_PROJECTS
echo ========================================================
echo   CONSTRUCTION DU PROJET (Maven Clean Install)
echo ========================================================
echo.
call mvn clean install -DskipTests
exit /b %ERRORLEVEL%

:EXIT
endlocal
exit
