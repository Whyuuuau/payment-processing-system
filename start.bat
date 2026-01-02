@echo off
REM Start Payment Processing System
REM Windows batch script

echo ====================================
echo Payment Processing System Startup
echo ====================================
echo.

REM Check if infrastructure is running
echo [1/4] Checking infrastructure services...

docker ps | findstr payment-postgres >nul
if errorlevel 1 (
    echo PostgreSQL not running. Starting Docker Compose...
    docker-compose up -d
    timeout /t 30 /nobreak
) else (
    echo Infrastructure services already running
)

echo.
echo [2/4] Verifying services health...

REM Check PostgreSQL
docker exec payment-postgres pg_isready -U payment_user -d payment_db >nul
if errorlevel 1 (
    echo ERROR: PostgreSQL not healthy
    exit /b 1
)
echo - PostgreSQL: OK

REM Check Redis
docker exec payment-redis redis-cli ping | findstr PONG >nul
if errorlevel 1 (
    echo ERROR: Redis not healthy
    exit /b 1
)
echo - Redis: OK

REM Check Kafka
docker exec payment-kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if errorlevel 1 (
    echo WARNING: Kafka not fully ready yet (this is normal on first start)
) else (
    echo - Kafka: OK
)

echo.
echo [3/4] Building application...
call mvn clean package -DskipTests
if errorlevel 1 (
    echo ERROR: Build failed
    exit /b 1
)

echo.
echo [4/4] Starting Payment Processing Application...
echo.
echo Application will start with optimized JVM settings:
echo - Heap: 2GB min, 4GB max
echo - GC: G1 Garbage Collector
echo - Max GC Pause: 200ms
echo.

java -Xms2G -Xmx4G ^
     -XX:+UseG1GC ^
     -XX:MaxGCPauseMillis=200 ^
     -XX:+HeapDumpOnOutOfMemoryError ^
     -XX:HeapDumpPath=logs/heapdump.hprof ^
     -XX:+ExitOnOutOfMemoryError ^
     -XX:NativeMemoryTracking=summary ^
     -jar payment-api/target/payment-api-1.0.0.jar

pause
