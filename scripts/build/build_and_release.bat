@echo off
cls
setlocal enabledelayedexpansion

rem import configuration
call config_build_and_release.bat

echo.
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo %BOLD%%BLUE% =                  BUILD AND RELEASE AUTOMATION TOOL                 = %RESET%
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo.

echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo %CYAN% ^|  CONFIGURATION                                                     ^| %RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
rem Check if the configuration was loaded
IF NOT "%CONFIG_LOAD%"=="true" (
    echo.
    echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Error loading the configuration file. Check config_build_and_release.bat file.%RESET%
    echo.
    EXIT /B 1
)

rem Log the configuration
echo  ^|  The working directory should be the same as the %BOLD%pom.xml%RESET% location:
echo  ^|  - %ITALIC%%CD%%RESET%
echo  ^|
echo  ^|  Selected free port: %GREEN%%PORT%%RESET%
echo  ^|  Starting application on: %UNDERLINE%%GREEN%%BASE_URL%%RESET%
echo  ^|  Arguments: %YELLOW%%MVN_ARGUMENTS%%RESET%
echo  ^|
echo  ^|  API Files: %GREEN%%API_COUNT%%RESET%
set /a INDEX=!API_COUNT!-1
for /L %%i in (0,1,%INDEX%) do (
    echo  ^|  - %MAGENTA%!API_FILES[%%i]!%RESET% [%CYAN%!API_ENDPOINTS[%%i]!%RESET%]
)
echo  ^|
echo  ^|  OpenAPI Configuration
    echo  ^|  - Project Name: %GREEN%%OPENAPI_PROJECT_NAME%%RESET%
    echo  ^|  - Tier1 Endpoints: %CYAN%%OPENAPI_TIER1_ENDPOINTS%%RESET%
    echo  ^|  - Tier2 Endpoints: %CYAN%%OPENAPI_TIER2_ENDPOINTS%%RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo.


:INIT
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo %BOLD%%BLUE% =                         AVAILABLE OPERATIONS                       = %RESET%
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo.
echo    %GREEN%1. Run the Spotless plugin (code formatting)%RESET%
echo    %GREEN%2. Run the build with tests (mvn clean package)%RESET%
echo    %GREEN%3. Start the application and generate the API docs (based on the configuration file)%RESET%
@REM echo    %GREEN%4. Processing the API docs (make the OpenAPI files ITB compliant)%RESET%
echo    %MAGENTA%4. Run all operations (required for the release)%RESET%
echo.
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo.

SET /P CHOICE=Enter the number of the desired operation:

IF "%CHOICE%"=="1" SET "ONLY_ONE=true" && GOTO SPOTLESS
IF "%CHOICE%"=="2" SET "ONLY_ONE=true" && GOTO BUILD
IF "%CHOICE%"=="3" SET "ONLY_ONE=true" && GOTO START_APP
@REM IF "%CHOICE%"=="4" SET "ONLY_ONE=true" && GOTO PROCESSING_DOCS
IF "%CHOICE%"=="4" GOTO RELEASE_WARNING

echo.
echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Invalid choice. Exiting.%RESET%
echo.
EXIT /B 1


:RELEASE_WARNING
echo.
echo %BOLD%%YELLOW% ====================================================================== %RESET%
echo %BOLD%%YELLOW% ^|                          RELEASE WARNING                           ^| %RESET%
echo %BOLD%%YELLOW% ====================================================================== %RESET%
echo.
echo  %RED%Before proceeding verify:%RESET%
echo.
echo    %YELLOW%- Version%RESET%        The version in the %YELLOW%%ITALIC%pipeline.variables.sh%RESET% file is updated
echo    %YELLOW%- Dependencies%RESET%   There are no references to %YELLOW%%ITALIC%SNAPSHOT%RESET% versions in the %YELLOW%%ITALIC%pom.xml%RESET% file
echo    %YELLOW%- Documentation%RESET%  Whether documentation updates are needed and update them accordingly (e.g. %YELLOW%%ITALIC%Upgrade Guide.md%RESET% and others if necessary)
echo.
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo.

SET /P CONFIRM=%MAGENTA%Do you want to proceed with the release? (y/n): %RESET%

IF "%CONFIRM%"=="y" SET "ONLY_ONE=false" && GOTO ALL
IF "%CONFIRM%"=="n" echo. && echo Release cancelled by user. && EXIT /B 1

echo.
echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Invalid response. Release cancelled.%RESET%
echo.
EXIT /B 1

:ALL
echo.
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo %BOLD%%BLUE% ^|                    EXECUTING ALL OPERATIONS                         ^| %RESET%
echo %BOLD%%BLUE% ====================================================================== %RESET%
echo.

:SPOTLESS
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo %CYAN% ^|  FORMATTING CODE WITH SPOTLESS                                     ^| %RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
call mvn spotless:apply || (
    echo.
    echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Spotless formatting failed!%RESET%
    echo.
    exit /B 1
)
if "%ONLY_ONE%"=="true" GOTO END

:BUILD
echo.
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo %CYAN% ^|  BUILDING THE APPLICATION                                          ^| %RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
call mvn clean package || (
    echo.
    echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Build failed!%RESET%
    echo.
    exit /B 1
)
if "%ONLY_ONE%"=="true" GOTO END

:START_APP
echo.
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo %CYAN% ^|  STARTING THE APPLICATION                                          ^| %RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
start "Spring Boot App" cmd /k "call mvn spring-boot:run %MVN_ARGUMENTS%"

echo  ^|  Waiting for the application to start on %UNDERLINE%%BASE_URL%%RESET%
set "HEALTH_URL=%BASE_URL%/actuator/health/liveness"
set "MAX_ATTEMPTS=30"
set /A "COUNT=0"

rem Clear the loader variable
set "LOADER="

:CHECK_HEALTH
rem Perform a request to the health endpoint
curl -s -o nul -w "%%{http_code}" %HEALTH_URL% | find "200" > nul
if %ERRORLEVEL% == 0 (
    echo.
    echo  %BG_BLUE%%BOLD% SUCCESS %RESET% %GREEN%+++ Application is up and running                              !%RESET%
    goto GENERATE_DOCS
)

rem Wait for 2 seconds before retrying
timeout /T 2 > nul
set /A "COUNT+=1"

set "LOADER=#%LOADER%"

for /f %%A in ('cmd /u /c "echo("') do set "BS=%%A"
for /f %%a in ('copy /Z "%~dpf0" nul') do set "CR=%%a"
<nul set /p"=!BS! ^| %YELLOW% Health check attempt: %COUNT%/%MAX_ATTEMPTS% %LOADER% %RESET% !CR!"


if %COUNT% LSS %MAX_ATTEMPTS% goto CHECK_HEALTH

echo.
echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Application did not start in time. Exiting...%RESET%
echo.
exit /B 1

:GENERATE_DOCS
echo.
echo %CYAN% ---------------------------------------------------------------------- %RESET%
echo %CYAN% ^|  GENERATING OPENAPI DOCUMENTATION                                  ^| %RESET%
echo %CYAN% ---------------------------------------------------------------------- %RESET%
set /a LAST_INDEX=!API_COUNT!-1
for /L %%i in (0,1,%LAST_INDEX%) do (
    echo.
    echo  ^|  Downloading %MAGENTA%!API_FILES[%%i]!%RESET% from %CYAN%!BASE_URL!!API_ENDPOINTS[%%i]!%RESET%
    curl -o !API_FILES[%%i]! !BASE_URL!!API_ENDPOINTS[%%i]! || (
        echo.
        echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- Failed to download OpenAPI file!%RESET%
        echo.
        exit /B 1
    )
)
echo.
echo  %BG_BLUE%%BOLD% SUCCESS %RESET% %GREEN%+++ OpenAPI JSON files generated successfully.%RESET%

rem Start split open api to convert OpenAPI JSON to YAML
powershell.exe -ExecutionPolicy Bypass -File "%~dp0config_split_openapi_by_tier.ps1" ^
    -projectName "%OPENAPI_PROJECT_NAME%" ^
    -tier1EndpointsString "%OPENAPI_TIER1_ENDPOINTS%" ^
    -tier2EndpointsString "%OPENAPI_TIER2_ENDPOINTS%"
if ERRORLEVEL 1 (
    echo.
    echo %BG_RED%%BOLD% ERROR %RESET% %RED%--- OpenAPI split by tiers failed!%RESET%
    echo.
    exit /B 1
)
rem Start script to convert OpenAPI JSON to YAML
powershell -ExecutionPolicy Bypass -File scripts/build/config_convert_openapi_json_to_yaml.ps1
if ERRORLEVEL 1 (
    echo.
    echo %BG_RED%%BOLD% ERROR %RESET% %RED%--- OpenAPI convert JsonToYaml failed!%RESET%
    echo.
    exit /B 1
)
if "%ONLY_ONE%"=="true" GOTO END

@REM :PROCESSING_DOCS
@REM echo.
@REM echo %CYAN% ---------------------------------------------------------------------- %RESET%
@REM echo %CYAN% ^|  PROCESSING OPENAPI FILES                                          ^| %RESET%
@REM echo %CYAN% ---------------------------------------------------------------------- %RESET%
@REM powershell -ExecutionPolicy Bypass -File scripts/build/config_process_openapi.ps1
@REM if ERRORLEVEL 1 (
@REM     echo  %BG_RED%%BOLD% ERROR %RESET% %RED%--- OpenAPI processing failed!%RESET%
@REM     echo.
@REM     exit /B 1
@REM )
@REM if "%ONLY_ONE%"=="true" GOTO END

:END
echo.
echo %BOLD%%GREEN% ====================================================================== %RESET%
echo %BOLD%%GREEN% ^|                       PROCESS COMPLETED                            ^| %RESET%
echo %BOLD%%GREEN% ====================================================================== %RESET%
echo.
echo  %BG_BLUE%%BOLD% SUCCESS %RESET% %GREEN%+++ BUILD AND RELEASE PROCESS COMPLETED SUCCESSFULLY!%RESET%
echo.

exit /B 0
