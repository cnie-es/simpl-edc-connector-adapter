rem Set the Maven arguments for starting the application
rem IMPORTANT: Spring Boot arguments must be space-separated and wrapped in quotes
rem Format: set "MVN_ARGUMENTS=-Dspring-boot.run.profiles=profile1,profile2 -Dspring-boot.run.arguments="--property1=value1 --property2=value2 --server.port={PORT}""
rem Use {PORT} as placeholder for server port
set "MVN_ARGUMENTS=-Dspring-boot.run.profiles=build -Dspring-boot.run.arguments="--server.port={PORT}""


rem Define openapi count based on the number of files
set "API_COUNT=2"
rem Define openapi files and endpoints
set "API_FILES[0]=openapi-v1.json"
set "API_ENDPOINTS[0]=/api-docs/v1"
set "API_FILES[1]=openapi-v2.json"
set "API_ENDPOINTS[1]=/api-docs/v2"

rem Project name used in output filenames (leave empty for default naming)
set "OPENAPI_PROJECT_NAME=edc-connector-adapter"

rem  ----------------------------------------------------------------------
rem  ATTENTION: DO NOT CHANGE THE FOLLOWING CODE
rem  ----------------------------------------------------------------------

rem Do not modify, it is used to verify whether the configuration is read correctly.
set "CONFIG_LOAD=true"


rem Set the working directory to go back 2 directories from the script location
rem This allows the script to run from /scripts/build but work 2 directories up

rem Get the directory where this script is located
set "SCRIPT_DIR=%~dp0"
rem Remove the trailing backslash
set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

rem Go back 2 directories from script location
for %%i in ("%SCRIPT_DIR%") do set "PARENT_DIR=%%~dpi"
set "PARENT_DIR=%PARENT_DIR:~0,-1%"
for %%i in ("%PARENT_DIR%") do set "WORKING_DIR=%%~dpi"
set "WORKING_DIR=%WORKING_DIR:~0,-1%"

rem Change to the working directory
cd /d "%WORKING_DIR%"


:findPort
REM Generate random port between 1024 and 65535
set /a PORT=1024 + (%RANDOM% * (65535 - 1024) / 32767)

REM Check if port is in use with netstat
netstat -ano | findstr /R ":!PORT! " >nul
if %errorlevel%==0 (
    REM Port is in use, try again
    goto findPort
) else (
    REM Port is free, set the variable
    set "BASE_URL=http://localhost:%PORT%"
    REM Replace {PORT} placeholder with actual port
    call set "MVN_ARGUMENTS=%%MVN_ARGUMENTS:{PORT}=%PORT%%%"
)

rem Define ANSI escape codes for colored output
for /F %%a in ('echo prompt $E ^| cmd') do (
  set "ESC=%%a"
)

set "ESC=%ESC%"
set "RESET=%ESC%[0m"
set "BOLD=%ESC%[1m"
set "ITALIC=%ESC%[3m"
set "UNDERLINE=%ESC%[4m"
set "RED=%ESC%[31m"
set "GREEN=%ESC%[32m"
set "YELLOW=%ESC%[33m"
set "BLUE=%ESC%[34m"
set "MAGENTA=%ESC%[35m"
set "CYAN=%ESC%[36m"
set "BG_RED=%ESC%[41m"
set "BG_BLUE=%ESC%[44m"
