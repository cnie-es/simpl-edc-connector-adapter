@ECHO off
cls
echo.
echo  ======================================================================
echo  =                   DOCKER BUILD AUTOMATION SCRIPT                    =
echo  ======================================================================
echo.

set servicename=edc-connector-adapter

@REM Apply image name and container name
set imagename=%servicename%-img
set containername=%servicename%-cnt

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

echo  ----------------------------------------------------------------------
echo  ^|  CONFIGURATION                                                     ^|
echo  ----------------------------------------------------------------------
echo  ^|  WORKING DIR   : %CD%
echo  ^|  SERVICE NAME  : %servicename%
echo  ^|  IMAGE NAME    : %imagename%
echo  ^|  CONTAINER NAME: %containername%
echo  ----------------------------------------------------------------------
echo.

echo  ----------------------------------------------------------------------
echo  ^|  REMOVING PREVIOUS CONTAINER                                       ^|
echo  ----------------------------------------------------------------------
call docker rm -f %containername%
echo.

echo  ----------------------------------------------------------------------
echo  ^|  REMOVING PREVIOUS IMAGE                                           ^|
echo  ----------------------------------------------------------------------
call docker rmi -f %imagename%
echo.

echo  ----------------------------------------------------------------------
echo  ^|  BUILDING DOCKER IMAGE                                             ^|
echo  ----------------------------------------------------------------------
call docker build -t %imagename% .
echo.

echo  ----------------------------------------------------------------------
echo  ^|  STARTING CONTAINER                                                ^|
echo  ----------------------------------------------------------------------
call docker run -d --name %containername% -p 8080:8080 %imagename%
echo.

echo  ======================================================================
echo  ^|                      OPERATION COMPLETED                            ^|
echo  ======================================================================
echo.

@REM Use PowerShell for show warnings messages
echo  ----------------------------------------------------------------------
powershell -Command "Write-Host ' WARNING:' -ForegroundColor Black -BackgroundColor Yellow -NoNewline; Write-Host ' If you get a Docker error like' -ForegroundColor Yellow -NoNewline; Write-Host $([char]27 + '[3m')'entrypoint.sh not found'$([char]27 + '[23m') -ForegroundColor Yellow"
powershell -Command "Write-Host '          make sure the file is formatted as Unix:' -ForegroundColor Yellow"
powershell -Command "Write-Host '          - IntelliJ IDEA: File -> File Properties -> Line Separators -> LF - Unix' -ForegroundColor Yellow"
powershell -Command "Write-Host '          - Eclipse IDE: File -> Convert Line Delimiters To -> Unix' -ForegroundColor Yellow"
echo  ----------------------------------------------------------------------
echo.

EXIT /B

