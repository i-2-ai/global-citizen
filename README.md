# Global Citizen - Secure Passport & Visa System

## Overview

The Global Citizen system is a revolutionary digital passport and visa platform that utilizes quantum-resistant cryptography to provide the most secure identity verification system in the world. This system replaces physical passports and visas with cryptographically signed JWT tokens displayed as QR codes.

## Architecture

### Cryptographic Foundation
- **Mother Key**: Quantum-resistant asymmetric key pair (CRYSTALS-Kyber) stored in the most secure location
- **Global Keys 1-7**: Intermediary keys signed by the mother key for country certificate signing
- **Country Root Keys**: Individual country keys signed by global keys
- **Citizen JWTs**: Passport information encoded as signed JWT tokens
- **Visa JWTs**: Visa information encoded as signed JWT tokens

### System Components

1. **Central Authority System**
   - Mother key generation and management
   - Global keys 1-7 generation and signing
   - Country certificate signing requests

2. **Country Management System**
   - Passport/JWT issuance to citizens
   - Citizen verification workflows
   - Public key publication

3. **Embassy System**
   - Visa/JWT issuance
   - Visa verification and approval workflows

4. **Immigration System**
   - QR code scanning and JWT validation
   - Real-time passport and visa verification

## Security Features

- **Quantum-Resistant Cryptography**: CRYSTALS-Kyber algorithm for future-proof security
- **Multi-Level Key Hierarchy**: Mother → Global → Country → Citizen chain of trust
- **JWT-Based Tokens**: Self-contained, verifiable identity documents
- **QR Code Interface**: Easy scanning and verification
- **Real-Time Validation**: Instant verification at ports of entry

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.x, Spring Security
- **Cryptography**: BouncyCastle, CRYSTALS-Kyber implementation
- **Database**: PostgreSQL with encrypted storage
- **Frontend**: React with TypeScript
- **API Documentation**: OpenAPI 3.0 (Swagger)
- **Testing**: JUnit 5, Mockito, TestContainers

## Project Structure

```
global-citizen/
├── backend/                 # Spring Boot backend services
│   ├── central-authority/   # Mother key and global key management
│   ├── country-service/     # Country passport issuance
│   ├── embassy-service/     # Embassy visa issuance
│   ├── immigration-service/ # Immigration verification
│   └── shared/             # Common libraries and utilities
├── frontend/               # React frontend applications
│   ├── central-admin/      # Central authority management UI
│   ├── country-admin/      # Country passport issuance UI
│   ├── embassy-admin/      # Embassy visa issuance UI
│   └── immigration-ui/     # Immigration verification UI
├── docs/                   # Documentation and API specs
└── docker/                 # Docker configurations
```

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 15+

### Installation

1. Clone the repository:
```bash
git clone https://github.com/your-org/global-citizen.git
cd global-citizen
```

2. Start the infrastructure:
```bash
docker-compose up -d postgres redis
```

3. Start the backend services:
```bash
./gradlew bootRun
```

4. Start the frontend applications:
```bash
cd frontend
npm install
npm start
```

## API Documentation

- Central Authority API: http://localhost:8081/swagger-ui.html
- Country Service API: http://localhost:8082/swagger-ui.html
- Embassy Service API: http://localhost:8083/swagger-ui.html
- Immigration Service API: http://localhost:8084/swagger-ui.html

## Security Considerations

- All cryptographic operations use quantum-resistant algorithms
- Keys are stored in Hardware Security Modules (HSM) in production
- All communications are encrypted with TLS 1.3
- JWT tokens have short expiration times
- Rate limiting and DDoS protection implemented
- Audit logging for all operations

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions, please contact the development team or create an issue in the repository. 