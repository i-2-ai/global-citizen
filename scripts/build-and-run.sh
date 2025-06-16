#!/bin/bash

# Global Citizen - Build and Run Script
# This script builds and runs the entire Global Citizen application using Docker Compose

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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

# Function to check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    print_success "Docker is running"
}

# Function to check if Docker Compose is available
check_docker_compose() {
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed. Please install Docker Compose and try again."
        exit 1
    fi
    print_success "Docker Compose is available"
}

# Function to build all services
build_services() {
    print_status "Building all services..."
    
    # Build shared module first
    print_status "Building shared module..."
    cd shared
    ./gradlew build -x test
    cd ..
    
    # Build all services
    services=("central-authority" "country-service" "embassy-service" "immigration-service" "ui")
    
    for service in "${services[@]}"; do
        print_status "Building $service..."
        cd "$service"
        ./gradlew build -x test
        cd ..
    done
    
    print_success "All services built successfully"
}

# Function to start the application
start_application() {
    local environment=${1:-production}
    
    print_status "Starting Global Citizen application in $environment mode..."
    
    if [ "$environment" = "dev" ]; then
        docker-compose -f docker-compose.dev.yml up -d
        print_success "Development environment started"
        print_status "Access points:"
        echo "  - UI: http://localhost:8084"
        echo "  - Central Authority API: http://localhost:8080"
        echo "  - Country Service API: http://localhost:8081"
        echo "  - Embassy Service API: http://localhost:8082"
        echo "  - Immigration Service API: http://localhost:8083"
        echo "  - pgAdmin: http://localhost:5050 (admin@globalcitizen.dev / admin123)"
        echo "  - Redis Commander: http://localhost:8081"
        echo "  - Jaeger: http://localhost:16686"
    else
        docker-compose up -d
        print_success "Production environment started"
        print_status "Access points:"
        echo "  - UI: http://localhost:8084"
        echo "  - Central Authority API: http://localhost:8080"
        echo "  - Country Service API: http://localhost:8081"
        echo "  - Embassy Service API: http://localhost:8082"
        echo "  - Immigration Service API: http://localhost:8083"
    fi
}

# Function to stop the application
stop_application() {
    local environment=${1:-production}
    
    print_status "Stopping Global Citizen application..."
    
    if [ "$environment" = "dev" ]; then
        docker-compose -f docker-compose.dev.yml down
    else
        docker-compose down
    fi
    
    print_success "Application stopped"
}

# Function to show application status
show_status() {
    local environment=${1:-production}
    
    print_status "Checking application status..."
    
    if [ "$environment" = "dev" ]; then
        docker-compose -f docker-compose.dev.yml ps
    else
        docker-compose ps
    fi
}

# Function to show logs
show_logs() {
    local service=${1:-""}
    local environment=${2:-production}
    
    if [ -z "$service" ]; then
        print_status "Showing logs for all services..."
        if [ "$environment" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml logs -f
        else
            docker-compose logs -f
        fi
    else
        print_status "Showing logs for $service..."
        if [ "$environment" = "dev" ]; then
            docker-compose -f docker-compose.dev.yml logs -f "$service"
        else
            docker-compose logs -f "$service"
        fi
    fi
}

# Function to clean up
cleanup() {
    print_status "Cleaning up Docker resources..."
    
    # Stop and remove containers
    docker-compose down --volumes --remove-orphans
    docker-compose -f docker-compose.dev.yml down --volumes --remove-orphans
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    print_success "Cleanup completed"
}

# Function to show help
show_help() {
    echo "Global Citizen - Build and Run Script"
    echo ""
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo ""
    echo "Commands:"
    echo "  build                    Build all services"
    echo "  start [env]              Start the application (env: production|dev, default: production)"
    echo "  stop [env]               Stop the application (env: production|dev, default: production)"
    echo "  restart [env]            Restart the application (env: production|dev, default: production)"
    echo "  status [env]             Show application status (env: production|dev, default: production)"
    echo "  logs [service] [env]     Show logs (env: production|dev, default: production)"
    echo "  cleanup                  Clean up Docker resources"
    echo "  help                     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 build                 Build all services"
    echo "  $0 start dev             Start development environment"
    echo "  $0 start                 Start production environment"
    echo "  $0 logs ui dev           Show logs for UI service in development"
    echo "  $0 cleanup               Clean up all Docker resources"
}

# Main script logic
case "${1:-help}" in
    "build")
        check_docker
        check_docker_compose
        build_services
        ;;
    "start")
        check_docker
        check_docker_compose
        start_application "${2:-production}"
        ;;
    "stop")
        check_docker
        check_docker_compose
        stop_application "${2:-production}"
        ;;
    "restart")
        check_docker
        check_docker_compose
        stop_application "${2:-production}"
        sleep 2
        start_application "${2:-production}"
        ;;
    "status")
        check_docker
        check_docker_compose
        show_status "${2:-production}"
        ;;
    "logs")
        check_docker
        check_docker_compose
        show_logs "${2:-}" "${3:-production}"
        ;;
    "cleanup")
        check_docker
        check_docker_compose
        cleanup
        ;;
    "help"|*)
        show_help
        ;;
esac 