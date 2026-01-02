@echo off
REM Git Setup Script for Payment Processing System
REM Initializes Git repository and prepares for GitHub

echo ====================================
echo Git Repository Setup
echo ====================================
echo.

REM Check if git is installed
git --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Git is not installed!
    echo Please download and install Git from: https://git-scm.com/download/win
    pause
    exit /b 1
)

echo [1/6] Git detected successfully
echo.

REM Initialize git repository if not exists
if not exist ".git" (
    echo [2/6] Initializing Git repository...
    git init
    echo Repository initialized!
) else (
    echo [2/6] Git repository already exists
)

echo.
echo [3/6] Configuring Git user (if not set globally)...
REM Check if user name is set
git config user.name >nul 2>&1
if errorlevel 1 (
    set /p GIT_NAME="Enter your name: "
    git config user.name "%GIT_NAME%"
)

REM Check if user email is set
git config user.email >nul 2>&1
if errorlevel 1 (
    set /p GIT_EMAIL="Enter your email: "
    git config user.email "%GIT_EMAIL%"
)

echo Git user: 
git config user.name
echo Git email: 
git config user.email
echo.

echo [4/6] Adding files to Git...
git add .
echo Files added to staging area
echo.

echo [5/6] Creating initial commit...
git commit -m "Initial commit: Payment Processing System with Spring Boot

- Multi-module Maven project (5 modules)
- Java 21 with Virtual Threads
- PostgreSQL + Redis + Kafka integration
- Resilience4j (Circuit Breaker, Retry, Rate Limiter, Bulkhead)
- Optimistic locking and idempotency protection
- Event sourcing for audit trail
- HikariCP connection pooling
- Docker Compose for infrastructure
- Comprehensive documentation"

if errorlevel 1 (
    echo.
    echo NOTE: If no changes detected, commit was skipped
) else (
    echo Initial commit created!
)

echo.
echo [6/6] Setup complete!
echo.
echo ====================================
echo Next Steps:
echo ====================================
echo.
echo 1. Create a new repository on GitHub:
echo    https://github.com/new
echo.
echo 2. Copy the repository URL (HTTPS or SSH)
echo.
echo 3. Add remote and push:
echo    git remote add origin https://github.com/YOUR_USERNAME/payment-processing-system.git
echo    git branch -M main
echo    git push -u origin main
echo.
echo ====================================
echo.

pause
