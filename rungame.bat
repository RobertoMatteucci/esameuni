@echo off
TITLE Fix-It Felix Jr. - Auto Builder

echo ==========================================
echo      FIX-IT FELIX JR. BUILD SCRIPT
echo ==========================================
echo.

:: 1. Create BIN directory if it doesn't exist
if not exist "bin" (
    echo [+] Creating bin directory...
    mkdir bin
)

:: 2. Compile the Java files
echo [+] Compiling Java source files...
echo    (This might take a few seconds)
javac -encoding UTF-8 -d bin -sourcepath src src/com/game/*.java

:: Check for compilation errors
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] COMPILATION FAILED!
    echo     Please check the error messages above.
    pause
    exit /b
)

echo [+] Compilation Successful!
echo.

:: 3. Run the Game
echo [+] Launching Game...
echo ==========================================
java -cp bin com.game.Main

pause