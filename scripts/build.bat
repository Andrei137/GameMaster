@echo off
cls
cd ..
echo Building the project...
javac -Xlint:deprecation -Xlint:unchecked -cp .;lib/* src\*.java src\exceptions\*.java src\interfaces\*.java src\misc\*.java src\models\*.java src\repository\*.java src\services\*.java
if %errorlevel% neq 0 (
    echo Build failed. Check the error messages above.
    cd scripts
    exit /b 1
) 
echo Project built successfully...
cd scripts
set /p runChoice=Do you want to run the project? (y/n): 
if /i "%runChoice%"=="y" (
    run.bat
)
