# Global Citizen - Docker Deployment Guide

This guide provides comprehensive instructions for deploying and running the Global Citizen application using Docker and Docker Compose.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Architecture Overview](#architecture-overview)
- [Configuration](#configuration)
- [Development Environment](#development-environment)
- [Production Deployment](#production-deployment)
- [Monitoring and Logging](#monitoring-and-logging)
- [Troubleshooting](#troubleshooting)
- [Security Considerations](#security-considerations)

## Prerequisites

Before running the Global Citizen application, ensure you have the following installed:

- **Docker** (version 20.10 or higher)
- **Docker Compose** (version 2.0 or higher)
- **Java 17** (for building the application)
- **Gradle** (for building the application)

### Installing Docker

#### Windows
1. Download Docker Desktop from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Install and start Docker Desktop
3. Ensure WSL 2 is enabled if prompted

#### macOS
1. Download Docker Desktop from [https://www.docker.com/products/docker-desktop](https://www.docker.com/products/docker-desktop)
2. Install and start Docker Desktop

#### Linux (Ubuntu/Debian)
```bash
# Update package index
sudo apt-get update

# Install prerequisites
sudo apt-get install apt-transport-https ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker repository
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io

# Add user to docker group
sudo usermod -aG docker $USER

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker
```

## Quick Start

### 1. Clone the Repository
```bash
git clone <repository-url>
cd global-citizen
```

### 2. Build and Start the Application
```bash
# Make the build script executable
chmod +x scripts/build-and-run.sh

# Build all services
./scripts/build-and-run.sh build

# Start the application in production mode
./scripts/build-and-run.sh start

# Or start in development mode
./scripts/build-and-run.sh start dev
```

### 3. Access the Application
Once started, you can access the application at:

- **UI Dashboard**: http://localhost:8084
- **Central Authority API**: http://localhost:8080
- **Country Service API**: http://localhost:8081
- **Embassy Service API**: http://localhost:8082
- **Immigration Service API**: http://localhost:8083

## Architecture Overview

The Global Citizen application is containerized using Docker with the following architecture:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Nginx Proxy   │    │   PostgreSQL    │    │     Redis       │
│   (Port 80/443) │    │   (Port 5432)   │    │   (Port 6379)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   UI Service    │    │ Central Authority│    │  Country Service│
│   (Port 8084)   │    │   (Port 8080)   │    │   (Port 8081)   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐
│ Embassy Service │    │Immigration Service│
│   (Port 8082)   │    │   (Port 8083)   │
└─────────────────┘    └─────────────────┘
```

### Service Dependencies

1. **Databases** (PostgreSQL instances)
   - Central Authority DB
   - Country Service DB
   - Embassy Service DB

2. **Cache** (Redis)
   - Shared caching layer

3. **Core Services**
   - Central Authority (Key management)
   - Country Service (Passport management)
   - Embassy Service (Visa management)
   - Immigration Service (Verification)

4. **UI Layer**
   - Web interface for all services

5. **Proxy** (Nginx)
   - Load balancing and routing

## Configuration

### Environment Variables

The application uses environment variables for configuration. Key variables include:

#### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-central:5432/global_citizen_central
SPRING_DATASOURCE_USERNAME=globalcitizen
SPRING_DATASOURCE_PASSWORD=secure_password_123
```

#### Redis Configuration
```bash
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

#### Service URLs
```bash
CENTRAL_AUTHORITY_URL=http://central-authority:8080
COUNTRY_SERVICE_URL=http://country-service:8081
EMBASSY_SERVICE_URL=http://embassy-service:8082
IMMIGRATION_SERVICE_URL=http://immigration-service:8083
```

### Customizing Configuration

To customize the configuration:

1. **Create environment-specific files**:
   ```bash
   cp docker-compose.yml docker-compose.custom.yml
   ```

2. **Modify environment variables** in the custom compose file

3. **Use the custom configuration**:
   ```bash
   docker-compose -f docker-compose.custom.yml up -d
   ```

## Development Environment

The development environment includes additional tools for debugging and development:

### Development Tools

- **pgAdmin**: Database management interface (http://localhost:5050)
- **Redis Commander**: Redis management interface (http://localhost:8081)
- **Jaeger**: Distributed tracing (http://localhost:16686)

### Starting Development Environment

```bash
# Start development environment
./scripts/build-and-run.sh start dev

# View logs for a specific service
./scripts/build-and-run.sh logs ui dev

# Check status
./scripts/build-and-run.sh status dev
```

### Development Features

- **Hot reloading** for code changes
- **Debug logging** enabled
- **Volume mounts** for live code editing
- **Development databases** with sample data

## Production Deployment

### Production Considerations

1. **Security**:
   - Use strong passwords
   - Enable HTTPS
   - Configure firewall rules
   - Use secrets management

2. **Performance**:
   - Configure resource limits
   - Enable caching
   - Use production-grade databases

3. **Monitoring**:
   - Set up health checks
   - Configure logging
   - Monitor resource usage

### Production Deployment Steps

1. **Build production images**:
   ```bash
   ./scripts/build-and-run.sh build
   ```

2. **Configure production environment**:
   ```bash
   # Create production environment file
   cp .env.example .env.production
   # Edit .env.production with production values
   ```

3. **Start production environment**:
   ```bash
   ./scripts/build-and-run.sh start
   ```

4. **Verify deployment**:
   ```bash
   ./scripts/build-and-run.sh status
   ```

### SSL/HTTPS Configuration

To enable HTTPS in production:

1. **Generate SSL certificates**:
   ```bash
   mkdir -p nginx/ssl
   # Generate self-signed certificates for testing
   openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
     -keyout nginx/ssl/key.pem \
     -out nginx/ssl/cert.pem
   ```

2. **Update Nginx configuration** in `nginx/nginx.conf`

3. **Restart the application**:
   ```bash
   ./scripts/build-and-run.sh restart
   ```

## Monitoring and Logging

### Health Checks

All services include health check endpoints:

- **Central Authority**: http://localhost:8080/actuator/health
- **Country Service**: http://localhost:8081/actuator/health
- **Embassy Service**: http://localhost:8082/actuator/health
- **Immigration Service**: http://localhost:8083/actuator/health
- **UI Service**: http://localhost:8084/actuator/health

### Viewing Logs

```bash
# View all logs
./scripts/build-and-run.sh logs

# View logs for specific service
./scripts/build-and-run.sh logs central-authority

# Follow logs in real-time
./scripts/build-and-run.sh logs -f
```

### Monitoring Commands

```bash
# Check container status
docker ps

# Check resource usage
docker stats

# Check network connectivity
docker network ls
docker network inspect global-citizen_global-citizen-network
```

## Troubleshooting

### Common Issues

#### 1. Port Conflicts
**Problem**: Services fail to start due to port conflicts
**Solution**: 
```bash
# Check what's using the ports
netstat -tulpn | grep :8080
# Stop conflicting services or change ports in docker-compose.yml
```

#### 2. Database Connection Issues
**Problem**: Services can't connect to databases
**Solution**:
```bash
# Check database containers
docker ps | grep postgres
# Check database logs
docker logs global-citizen-postgres-central
# Restart database containers
docker-compose restart postgres-central
```

#### 3. Memory Issues
**Problem**: Containers run out of memory
**Solution**:
```bash
# Increase Docker memory limit in Docker Desktop settings
# Or add memory limits to docker-compose.yml
```

#### 4. Build Failures
**Problem**: Services fail to build
**Solution**:
```bash
# Clean and rebuild
./scripts/build-and-run.sh cleanup
./scripts/build-and-run.sh build
```

### Debug Commands

```bash
# Enter a running container
docker exec -it global-citizen-central-authority bash

# Check service logs
docker logs global-citizen-central-authority

# Check network connectivity
docker exec global-citizen-central-authority ping postgres-central

# Check environment variables
docker exec global-citizen-central-authority env
```

## Security Considerations

### Production Security Checklist

- [ ] Change default passwords
- [ ] Enable HTTPS/TLS
- [ ] Configure firewall rules
- [ ] Use secrets management
- [ ] Enable audit logging
- [ ] Regular security updates
- [ ] Backup strategy
- [ ] Disaster recovery plan

### Security Best Practices

1. **Use secrets management**:
   ```bash
   # Create secrets
   echo "secure_password_123" | docker secret create db_password -
   ```

2. **Enable security scanning**:
   ```bash
   # Scan images for vulnerabilities
   docker scan global-citizen-central-authority
   ```

3. **Regular updates**:
   ```bash
   # Update base images
   docker-compose pull
   docker-compose build --no-cache
   ```

### Network Security

The application uses a custom Docker network (`global-citizen-network`) for internal communication. External access is only available through the Nginx proxy.

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Redis Docker Image](https://hub.docker.com/_/redis)

## Support

For issues and questions:

1. Check the troubleshooting section above
2. Review the application logs
3. Check the GitHub issues page
4. Contact the development team

---

**Note**: This is a development and demonstration system. For production use, ensure proper security measures, monitoring, and backup strategies are in place. 