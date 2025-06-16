# Global Citizen - End-to-End Tests

This directory contains comprehensive end-to-end tests for the Global Citizen application that simulate real-world scenarios from key creation to immigration verification.

## Test Scenarios

### 1. Complete Travel Journey Test
- **File**: `CompleteTravelJourneyTest.java`
- **Description**: Simulates the complete journey of a citizen from passport issuance to immigration verification
- **Steps**:
  1. Central Authority creates mother key and global keys
  2. Country A issues a digital passport to a citizen
  3. Embassy B issues a visa to the same citizen
  4. Citizen travels to Country B
  5. Immigration verifies both passport and visa using QR codes

### 2. Multi-Country Visa Test
- **File**: `MultiCountryVisaTest.java`
- **Description**: Tests multiple countries issuing visas to the same traveler
- **Steps**:
  1. Multiple embassies issue visas to a single traveler
  2. Immigration verifies all visas
  3. Tests visa entry/exit tracking

### 3. Security Verification Test
- **File**: `SecurityVerificationTest.java`
- **Description**: Tests security features and edge cases
- **Steps**:
  1. Tests expired documents
  2. Tests revoked documents
  3. Tests invalid signatures
  4. Tests watchlist matching

## Test Structure

```
e2e-tests/
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── globalcitizen/
│                   └── e2e/
│                       ├── CompleteTravelJourneyTest.java
│                       ├── MultiCountryVisaTest.java
│                       ├── SecurityVerificationTest.java
│                       ├── TestData.java
│                       └── TestUtils.java
├── build.gradle
├── docker-compose.test.yml
└── README.md
```

## Prerequisites

- Docker and Docker Compose
- Java 17
- Gradle
- All Global Citizen services running

## Running the Tests

### 1. Start the Application
```bash
# From the root directory
./scripts/build-and-run.sh start dev
```

### 2. Run End-to-End Tests
```bash
# Navigate to e2e-tests directory
cd e2e-tests

# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests CompleteTravelJourneyTest

# Run with detailed output
./gradlew test --info
```

### 3. Run Tests with Docker
```bash
# Start test environment
docker-compose -f docker-compose.test.yml up -d

# Run tests
./gradlew test

# Stop test environment
docker-compose -f docker-compose.test.yml down
```

## Test Configuration

### Environment Variables
```bash
# Service URLs
CENTRAL_AUTHORITY_URL=http://localhost:8080
COUNTRY_SERVICE_URL=http://localhost:8081
EMBASSY_SERVICE_URL=http://localhost:8082
IMMIGRATION_SERVICE_URL=http://localhost:8083
UI_SERVICE_URL=http://localhost:8084

# Test Configuration
TEST_TIMEOUT=30000
TEST_RETRY_COUNT=3
```

### Test Data
The tests use predefined test data from `TestData.java`:
- Sample citizens with realistic information
- Sample countries and embassies
- Test keys and certificates

## Test Reports

After running tests, reports are generated in:
- **HTML Reports**: `build/reports/tests/test/index.html`
- **JUnit XML**: `build/test-results/test/`
- **Coverage**: `build/reports/jacoco/test/html/index.html`

## Debugging Tests

### 1. Enable Debug Logging
```bash
# Add to test environment
LOGGING_LEVEL_COM_GLOBALCITIZEN=DEBUG
```

### 2. View Service Logs
```bash
# View logs during test execution
docker-compose logs -f central-authority
docker-compose logs -f country-service
docker-compose logs -f embassy-service
docker-compose logs -f immigration-service
```

### 3. Check Database State
```bash
# Connect to test databases
docker exec -it global-citizen-postgres-central psql -U globalcitizen -d global_citizen_central
docker exec -it global-citizen-postgres-country psql -U globalcitizen -d global_citizen_country
docker exec -it global-citizen-postgres-embassy psql -U globalcitizen -d global_citizen_embassy
```

## Test Utilities

### TestUtils.java
Provides helper methods for:
- Service health checks
- Database cleanup
- JWT token generation
- QR code generation
- API client creation

### TestData.java
Contains test data constants:
- Citizen information
- Country codes
- Embassy information
- Sample keys and certificates

## Continuous Integration

The tests can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
- name: Run E2E Tests
  run: |
    cd e2e-tests
    ./gradlew test
```

## Troubleshooting

### Common Issues

1. **Service Not Available**
   - Check if all services are running: `docker ps`
   - Verify health endpoints: `curl http://localhost:8080/actuator/health`

2. **Database Connection Issues**
   - Check database containers: `docker logs global-citizen-postgres-central`
   - Verify database initialization scripts

3. **Test Timeouts**
   - Increase timeout in test configuration
   - Check service response times

4. **Port Conflicts**
   - Ensure no other services are using the required ports
   - Use different ports in test configuration

### Debug Commands

```bash
# Check test environment
docker-compose -f docker-compose.test.yml ps

# View test logs
docker-compose -f docker-compose.test.yml logs

# Clean test environment
docker-compose -f docker-compose.test.yml down -v
```

## Contributing

When adding new tests:

1. Follow the existing test structure
2. Use `TestData.java` for test data
3. Use `TestUtils.java` for common operations
4. Add appropriate assertions and error handling
5. Update this README with new test descriptions

## Test Coverage

The end-to-end tests cover:
- ✅ Key management workflow
- ✅ Passport issuance and verification
- ✅ Visa issuance and verification
- ✅ Immigration verification
- ✅ QR code generation and scanning
- ✅ JWT token validation
- ✅ Security features
- ✅ Error handling
- ✅ Performance under load 