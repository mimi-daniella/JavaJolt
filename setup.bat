@echo off
REM JavaJolt Quick Setup Batch Script
echo 🚀 JavaJolt Quick Setup
echo ========================

REM Check if .env exists
if not exist ".env" (
    echo ❌ Error: .env file not found!
    echo Please create a .env file with your configuration.
    pause
    exit /b 1
)

echo 📝 Loading environment variables from .env...
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if not "%%a"=="" if not "%%a:~0,1%"=="#" (
        set %%a=%%b
        echo   ✓ Set %%a
    )
)

echo ✅ Environment variables loaded!
echo.
echo 🚀 Starting JavaJolt application...
echo Application will be available at: http://localhost:8080/quiz-app
echo Press Ctrl+C to stop
echo.

REM Start the application
call .\mvnw.cmd spring-boot:run