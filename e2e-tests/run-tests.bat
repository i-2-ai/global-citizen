@echo off
REM Global Citizen End-to-End Test Runner for Windows
REM This script provides various options for running the end-to-end tests

setlocal enabledelayedexpansion

REM Default values
set TEST_ENVIRONMENT=local
set CLEANUP=true
set BUILD=true
set VERBOSE=false
set SPECIFIC_TEST=

REM Function to print colored output
:print_status
echo [INFO] %~1
goto :eof

:print_success
echo [SUCCESS] %~1
goto :eof

:print_warning
echo [WARNING] %~1
goto :eof

:print_error
echo [ERROR] %~1
goto :eof

REM Function to show usage
:show_usage
echo Global Citizen End-to-End Test Runner
echo.
echo Usage: %0 [OPTIONS]
echo.
echo Options:
echo   -e, --environment ENV    Test environment (local^|docker^|ci) [default: local]
echo   -t, --test TEST_NAME     Run specific test (CompleteTravelJourneyTest^|MultiCountryVisaTest^|SecurityVerificationTest)
echo   -c, --no-cleanup         Don't cleanup after tests
echo   -b, --no-build           Don't build before running tests
echo   -v, --verbose            Verbose output
echo   -h, --help               Show this help message
echo.
echo Examples:
echo   %0                                    # Run all tests locally
echo   %0 -e docker                          # Run all tests in Docker
echo   %0 -t CompleteTravelJourneyTest       # Run specific test
echo   %0 -e docker -v                       # Run tests in Docker with verbose output
goto :eof

REM Function to parse command line arguments
:parse_arguments
:parse_loop
if "%~1"=="" goto :eof
if "%~1"=="-e" (
    set TEST_ENVIRONMENT=%~2
    shift
    shift
    goto parse_loop
)
if "%~1"=="--environment" (
    set TEST_ENVIRONMENT=%~2
    shift
    shift
    goto parse_loop
)
if "%~1"=="-t" (
    set SPECIFIC_TEST=%~2
    shift
    shift
    goto parse_loop
)
if "%~1"=="--test" (
    set SPECIFIC_TEST=%~2
    shift
    shift
    goto parse_loop
)
if "%~1"=="-c" (
    set CLEANUP=false
    shift
    goto parse_loop
)
if "%~1"=="--no-cleanup" (
    set CLEANUP=false
    shift
    goto parse_loop
)
if "%~1"=="-b" (
    set BUILD=false
    shift
    goto parse_loop
)
if "%~1"=="--no-build" (
    set BUILD=false
    shift
    goto parse_loop
)
if "%~1"=="-v" (
    set VERBOSE=true
    shift
    goto parse_loop
)
if "%~1"=="--verbose" (
    set VERBOSE=true
    shift
    goto parse_loop
)
if "%~1"=="-h" (
    call :show_usage
    exit /b 0
)
if "%~1"=="--help" (
    call :show_usage
    exit /b 0
)
call :print_error "Unknown option: %~1"
call :show_usage
exit /b 1

REM Function to check prerequisites
:check_prerequisites
call :print_status "Checking prerequisites..."

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    call :print_error "Java is not installed. Please install Java 17 or later."
    exit /b 1
)

REM Check Java version (simplified check)
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%i
    set JAVA_VERSION=!JAVA_VERSION:"=!
    for /f "tokens=1 delims=." %%j in ("!JAVA_VERSION!") do set JAVA_MAJOR=%%j
    if !JAVA_MAJOR! lss 17 (
        call :print_error "Java 17 or later is required. Current version: !JAVA_MAJOR!"
        exit /b 1
    )
)

call :print_success "Java version: "
java -version 2>&1 | findstr /i "version"

REM Check if Docker is installed (for Docker environment)
if "%TEST_ENVIRONMENT%"=="docker" (
    docker --version >nul 2>&1
    if errorlevel 1 (
        call :print_error "Docker is not installed. Please install Docker."
        exit /b 1
    )
    
    docker-compose --version >nul 2>&1
    if errorlevel 1 (
        call :print_error "Docker Compose is not installed. Please install Docker Compose."
        exit /b 1
    )
    
    call :print_success "Docker version: "
    docker --version
    call :print_success "Docker Compose version: "
    docker-compose --version
)

call :print_success "Prerequisites check completed"
goto :eof

REM Function to build the project
:build_project
if "%BUILD%"=="true" (
    call :print_status "Building project..."
    
    if "%VERBOSE%"=="true" (
        gradlew.bat build --info
    ) else (
        gradlew.bat build
    )
    
    if errorlevel 1 (
        call :print_error "Build failed"
        exit /b 1
    )
    
    call :print_success "Build completed"
) else (
    call :print_warning "Skipping build"
)
goto :eof

REM Function to start local services
:start_local_services
call :print_status "Starting local services..."

REM Check if services are already running
curl -s http://localhost:8080/actuator/health >nul 2>&1
if not errorlevel 1 (
    call :print_warning "Services appear to be already running"
    goto :eof
)

REM Start services using the main docker-compose
cd ..
if "%VERBOSE%"=="true" (
    docker-compose up -d
) else (
    docker-compose up -d >nul 2>&1
)

REM Wait for services to be ready
call :print_status "Waiting for services to be ready..."
timeout /t 30 /nobreak >nul

REM Check service health
call :check_service_health

cd e2e-tests
call :print_success "Local services started"
goto :eof

REM Function to check service health
:check_service_health
call :print_status "Checking service health..."

set services[0]=http://localhost:8080/actuator/health
set services[1]=http://localhost:8081/actuator/health
set services[2]=http://localhost:8082/actuator/health
set services[3]=http://localhost:8083/actuator/health
set services[4]=http://localhost:8084/actuator/health

for %%i in (0,1,2,3,4) do (
    set retries=0
    set max_retries=30
    
    :health_check_loop
    curl -s !services[%%i]! >nul 2>&1
    if not errorlevel 1 (
        call :print_success "Service !services[%%i]! is healthy"
        goto :next_service
    ) else (
        set /a retries+=1
        if !retries! geq !max_retries! (
            call :print_error "Service !services[%%i]! failed to start"
            exit /b 1
        )
        timeout /t 2 /nobreak >nul
        goto :health_check_loop
    )
    :next_service
)
goto :eof

REM Function to run tests locally
:run_tests_local
call :print_status "Running tests locally..."

set gradle_args=test

if not "%SPECIFIC_TEST%"=="" (
    set gradle_args=test --tests %SPECIFIC_TEST%
)

if "%VERBOSE%"=="true" (
    set gradle_args=%gradle_args% --info
)

gradlew.bat %gradle_args%
if errorlevel 1 (
    call :print_error "Tests failed"
    exit /b 1
)

call :print_success "Tests completed successfully"
goto :eof

REM Function to run tests in Docker
:run_tests_docker
call :print_status "Running tests in Docker..."

REM Build and start test environment
if "%VERBOSE%"=="true" (
    docker-compose -f docker-compose.test.yml up --build
) else (
    docker-compose -f docker-compose.test.yml up --build >nul 2>&1
)

REM Wait for test completion
call :print_status "Waiting for tests to complete..."
docker-compose -f docker-compose.test.yml logs -f test-runner

REM Get test results
for /f %%i in ('docker-compose -f docker-compose.test.yml ps -q test-runner') do (
    for /f %%j in ('docker inspect -f "{{.State.ExitCode}}" %%i') do set exit_code=%%j
)

if "%exit_code%"=="0" (
    call :print_success "Tests completed successfully"
) else (
    call :print_error "Tests failed with exit code: %exit_code%"
    exit /b 1
)
goto :eof

REM Function to cleanup
:cleanup
if "%CLEANUP%"=="true" (
    call :print_status "Cleaning up..."
    
    if "%TEST_ENVIRONMENT%"=="docker" (
        docker-compose -f docker-compose.test.yml down -v
    ) else (
        cd ..
        docker-compose down
        cd e2e-tests
    )
    
    call :print_success "Cleanup completed"
) else (
    call :print_warning "Skipping cleanup"
)
goto :eof

REM Function to show test results
:show_test_results
call :print_status "Test Results:"

if exist "build\reports\tests\test" (
    echo HTML Report: file://%cd%\build\reports\tests\test\index.html
)

if exist "build\test-results\test" (
    echo JUnit XML: %cd%\build\test-results\test\
)

if exist "build\reports\jacoco\test\html" (
    echo Coverage Report: file://%cd%\build\reports\jacoco\test\html\index.html
)
goto :eof

REM Main function
:main
call :print_status "Global Citizen End-to-End Test Runner"
call :print_status "Environment: %TEST_ENVIRONMENT%"
if "%SPECIFIC_TEST%"=="" (
    call :print_status "Specific Test: All tests"
) else (
    call :print_status "Specific Test: %SPECIFIC_TEST%"
)
call :print_status "Cleanup: %CLEANUP%"
call :print_status "Build: %BUILD%"
call :print_status "Verbose: %VERBOSE%"
echo.

REM Parse arguments
call :parse_arguments %*

REM Check prerequisites
call :check_prerequisites

REM Build project
call :build_project

REM Start services based on environment
if "%TEST_ENVIRONMENT%"=="local" (
    call :start_local_services
    call :run_tests_local
) else if "%TEST_ENVIRONMENT%"=="docker" (
    call :run_tests_docker
) else if "%TEST_ENVIRONMENT%"=="ci" (
    REM CI environment - assume services are already running
    call :run_tests_local
) else (
    call :print_error "Unknown environment: %TEST_ENVIRONMENT%"
    exit /b 1
)

REM Show test results
call :show_test_results

REM Cleanup
call :cleanup

call :print_success "Test execution completed"
goto :eof

REM Run main function with all arguments
call :main %* 