#!/bin/bash

# Global Citizen End-to-End Test Runner
# This script provides various options for running the end-to-end tests

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
TEST_ENVIRONMENT="local"
CLEANUP=true
BUILD=true
VERBOSE=false
SPECIFIC_TEST=""

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "Global Citizen End-to-End Test Runner"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -e, --environment ENV    Test environment (local|docker|ci) [default: local]"
    echo "  -t, --test TEST_NAME     Run specific test (CompleteTravelJourneyTest|MultiCountryVisaTest|SecurityVerificationTest)"
    echo "  -c, --no-cleanup         Don't cleanup after tests"
    echo "  -b, --no-build           Don't build before running tests"
    echo "  -v, --verbose            Verbose output"
    echo "  -h, --help               Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Run all tests locally"
    echo "  $0 -e docker                          # Run all tests in Docker"
    echo "  $0 -t CompleteTravelJourneyTest       # Run specific test"
    echo "  $0 -e docker -v                       # Run tests in Docker with verbose output"
}

# Function to parse command line arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--environment)
                TEST_ENVIRONMENT="$2"
                shift 2
                ;;
            -t|--test)
                SPECIFIC_TEST="$2"
                shift 2
                ;;
            -c|--no-cleanup)
                CLEANUP=false
                shift
                ;;
            -b|--no-build)
                BUILD=false
                shift
                ;;
            -v|--verbose)
                VERBOSE=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
        esac
    done
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_error "Java is not installed. Please install Java 17 or later."
        exit 1
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt 17 ]; then
        print_error "Java 17 or later is required. Current version: $JAVA_VERSION"
        exit 1
    fi
    
    print_success "Java version: $(java -version 2>&1 | head -n 1)"
    
    # Check if Docker is installed (for Docker environment)
    if [ "$TEST_ENVIRONMENT" = "docker" ]; then
        if ! command -v docker &> /dev/null; then
            print_error "Docker is not installed. Please install Docker."
            exit 1
        fi
        
        if ! command -v docker-compose &> /dev/null; then
            print_error "Docker Compose is not installed. Please install Docker Compose."
            exit 1
        fi
        
        print_success "Docker version: $(docker --version)"
        print_success "Docker Compose version: $(docker-compose --version)"
    fi
    
    print_success "Prerequisites check completed"
}

# Function to build the project
build_project() {
    if [ "$BUILD" = true ]; then
        print_status "Building project..."
        
        if [ "$VERBOSE" = true ]; then
            ./gradlew build --info
        else
            ./gradlew build
        fi
        
        print_success "Build completed"
    else
        print_warning "Skipping build"
    fi
}

# Function to start local services
start_local_services() {
    print_status "Starting local services..."
    
    # Check if services are already running
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        print_warning "Services appear to be already running"
        return
    fi
    
    # Start services using the main docker-compose
    cd ..
    if [ "$VERBOSE" = true ]; then
        docker-compose up -d
    else
        docker-compose up -d > /dev/null 2>&1
    fi
    
    # Wait for services to be ready
    print_status "Waiting for services to be ready..."
    sleep 30
    
    # Check service health
    check_service_health
    
    cd e2e-tests
    print_success "Local services started"
}

# Function to check service health
check_service_health() {
    print_status "Checking service health..."
    
    local services=(
        "http://localhost:8080/actuator/health"
        "http://localhost:8081/actuator/health"
        "http://localhost:8082/actuator/health"
        "http://localhost:8083/actuator/health"
        "http://localhost:8084/actuator/health"
    )
    
    for service in "${services[@]}"; do
        local retries=0
        local max_retries=30
        
        while [ $retries -lt $max_retries ]; do
            if curl -s "$service" > /dev/null 2>&1; then
                print_success "Service $service is healthy"
                break
            else
                retries=$((retries + 1))
                if [ $retries -eq $max_retries ]; then
                    print_error "Service $service failed to start"
                    exit 1
                fi
                sleep 2
            fi
        done
    done
}

# Function to run tests locally
run_tests_local() {
    print_status "Running tests locally..."
    
    local gradle_args="test"
    
    if [ -n "$SPECIFIC_TEST" ]; then
        gradle_args="test --tests $SPECIFIC_TEST"
    fi
    
    if [ "$VERBOSE" = true ]; then
        gradle_args="$gradle_args --info"
    fi
    
    if ./gradlew $gradle_args; then
        print_success "Tests completed successfully"
    else
        print_error "Tests failed"
        exit 1
    fi
}

# Function to run tests in Docker
run_tests_docker() {
    print_status "Running tests in Docker..."
    
    # Build and start test environment
    if [ "$VERBOSE" = true ]; then
        docker-compose -f docker-compose.test.yml up --build
    else
        docker-compose -f docker-compose.test.yml up --build > /dev/null 2>&1
    fi
    
    # Wait for test completion
    print_status "Waiting for tests to complete..."
    docker-compose -f docker-compose.test.yml logs -f test-runner
    
    # Get test results
    local exit_code=$(docker-compose -f docker-compose.test.yml ps -q test-runner | xargs docker inspect -f '{{.State.ExitCode}}')
    
    if [ "$exit_code" = "0" ]; then
        print_success "Tests completed successfully"
    else
        print_error "Tests failed with exit code: $exit_code"
        exit 1
    fi
}

# Function to cleanup
cleanup() {
    if [ "$CLEANUP" = true ]; then
        print_status "Cleaning up..."
        
        if [ "$TEST_ENVIRONMENT" = "docker" ]; then
            docker-compose -f docker-compose.test.yml down -v
        else
            cd ..
            docker-compose down
            cd e2e-tests
        fi
        
        print_success "Cleanup completed"
    else
        print_warning "Skipping cleanup"
    fi
}

# Function to show test results
show_test_results() {
    print_status "Test Results:"
    
    if [ -d "build/reports/tests/test" ]; then
        echo "HTML Report: file://$(pwd)/build/reports/tests/test/index.html"
    fi
    
    if [ -d "build/test-results/test" ]; then
        echo "JUnit XML: $(pwd)/build/test-results/test/"
    fi
    
    if [ -d "build/reports/jacoco/test/html" ]; then
        echo "Coverage Report: file://$(pwd)/build/reports/jacoco/test/html/index.html"
    fi
}

# Main function
main() {
    print_status "Global Citizen End-to-End Test Runner"
    print_status "Environment: $TEST_ENVIRONMENT"
    print_status "Specific Test: ${SPECIFIC_TEST:-All tests}"
    print_status "Cleanup: $CLEANUP"
    print_status "Build: $BUILD"
    print_status "Verbose: $VERBOSE"
    echo ""
    
    # Parse arguments
    parse_arguments "$@"
    
    # Check prerequisites
    check_prerequisites
    
    # Build project
    build_project
    
    # Start services based on environment
    case $TEST_ENVIRONMENT in
        "local")
            start_local_services
            run_tests_local
            ;;
        "docker")
            run_tests_docker
            ;;
        "ci")
            # CI environment - assume services are already running
            run_tests_local
            ;;
        *)
            print_error "Unknown environment: $TEST_ENVIRONMENT"
            exit 1
            ;;
    esac
    
    # Show test results
    show_test_results
    
    # Cleanup
    cleanup
    
    print_success "Test execution completed"
}

# Run main function with all arguments
main "$@" 