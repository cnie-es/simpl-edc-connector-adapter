@echo off
setlocal enabledelayedexpansion

set "SERVICE_DESC=Minio"
set "LOCAL_PORT=9000"
set "SERVICE_PORT=9000"

title !SERVICE_DESC! - Kubernetes Service Port Forwarding

echo.
echo ========================================================
echo   !SERVICE_DESC! - Kubernetes Service Port Forwarding
echo ========================================================

REM ---[ Set environment variables with defaults ]---------
if "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT%"=="" (
    set "SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT=dev-components
"
    echo [INFO] Environment variable SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT not set.
    echo [INFO] Using default value: "!SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT!"
)

if "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE%"=="" (
    set "SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE=gaiax-dev-minio"
    echo [INFO] Environment variable SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE not set.
    echo [INFO] Using default value: "!SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE!"
)

if "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_SERVICE%"=="" (
    set "SIMPL_EDC_ADAPTER_CONSUMER_K8S_SERVICE=minio"
    echo [INFO] Environment variable SIMPL_EDC_ADAPTER_CONSUMER_K8S_SERVICE not set.
    echo [INFO] Using default value: "!SIMPL_EDC_ADAPTER_CONSUMER_K8S_SERVICE!"
)

REM ---[ Set Kubernetes context ]--------------------------
echo.
echo [INFO] Setting Kubernetes context to "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT%"...
kubectl config use-context "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT%" > nul 2>&1

if %errorlevel% neq 0 (
    echo [ERROR] Failed to set context "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT%".
    echo [INFO] Check available contexts using: kubectl config get-contexts
    echo [INFO] Auto closing in 5 seconds...
    timeout /t 5 /nobreak > nul
    exit /b
)

REM Check if port forwarding is already running on port LOCAL_PORT
netstat -ano | findstr :!LOCAL_PORT! | findstr LISTENING > nul
if %errorlevel% equ 0 (
    echo.
    echo [INFO] Port forwarding is already running on port !LOCAL_PORT!.
    echo.
    echo [INFO] Automatic closing in 5 seconds...
    timeout /t 5 /nobreak > nul
    exit
)

REM ---[ Start port forwarding ]---------------------------
:start
echo.
echo [INFO] Starting port forwarding for !SERVICE_DESC! Kubernetes service...
echo [INFO] Context: %SIMPL_EDC_ADAPTER_CONSUMER_K8S_CONTEXT%
echo [INFO] Namespace: %SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE%
echo [INFO] This script exposes the !SERVICE_DESC! Kubernetes service on local port !LOCAL_PORT!
echo [INFO] To access Swagger UI: http://localhost:!LOCAL_PORT!/swagger-ui/index.html
echo.

kubectl port-forward -n "%SIMPL_EDC_ADAPTER_CONSUMER_K8S_NAMESPACE%" services/%SIMPL_EDC_ADAPTER_CONSUMER_K8S_SERVICE% !LOCAL_PORT!:!SERVICE_PORT!

echo.
echo [INFO] Port forwarding terminated.
echo [INFO] Press any key to restart port forwarding or CTRL+C to exit.
echo.
pause > nul
goto start
