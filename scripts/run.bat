@echo off
setlocal

set build=False
if "%1"=="-b" (
    set build=True
)

if %build%==True (
    cls && call build.bat && goto run
) else (
    goto run
)
:end
endlocal
exit /b 0

:run
cd ..
java -cp .;bin;src;exceptions;interfaces;misc;models;repository;services;lib/commons-lang3-3.14.0.jar;lib/mysql-connector-java-8.3.0.jar;lib/jansi-2.4.0.jar src.Main
cd scripts
goto end
