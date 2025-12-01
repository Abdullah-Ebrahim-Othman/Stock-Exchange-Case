# 📈 Stock Exchange Management System

<div align="center">

[![Java](https://img.shields.io/badge/Java-17-3178C6?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16.0.3-black?style=for-the-badge&logo=next.js&logoColor=white)](https://nextjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Jib](https://img.shields.io/badge/Container-Jib-4285F4?style=for-the-badge&logo=docker&logoColor=white)](https://github.com/GoogleContainerTools/jib)
[![JWT](https://img.shields.io/badge/Auth-JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://github.com/jwtk/jjwt)

**A modern, production-ready Stock Exchange Management System with real-time trading capabilities**

[Features](#-features) • [Quick Start](#-quick-start) • [API Documentation](#-api-documentation) • [Architecture](#-architecture) • [Contributing](#-contributing)

---

</div>

## 🌟 Overview

A full-stack, enterprise-grade stock trading platform featuring real-time market data, secure authentication, and comprehensive portfolio management. Built with Spring Boot backend and Next.js frontend, containerized using Google Jib (backend) and Docker (frontend).

### ✨ What Makes This Special

- **Production Ready** - Built with enterprise patterns, security best practices, and comprehensive testing
- **Containerized with Jib** - Backend uses Google Jib for fast, daemon-less Docker builds
- **Type Safe** - Full TypeScript coverage on frontend with Zod validation
- **Secure by Design** - JWT authentication with HttpOnly cookies, BCrypt hashing, role-based access
- **Well Architected** - Clean separation of concerns, auditing, versioning, and optimistic locking
- **Developer Friendly** - OpenAPI/Swagger docs, MapStruct DTOs, Spring Actuator monitoring

---

## 🚀 Key Features

### 📊 App Management

- **Stock Exchange Management**
    - Multiple exchange support
    - Live market status tracking
    - Exchange-specific stock listings
    - **Bulk stock operations** (add/remove multiple stocks)

### 📈 Data Management

- **JPA Entities with Advanced Features**
    - Optimistic locking with `@Version`
    - Audit trails with `@EnableJpaAuditing`
    - Composite keys for junction tables
    - Custom repository queries with pagination

### 🛠 Developer Experience

- **OpenAPI Documentation**
    - Interactive Swagger UI at `/swagger-ui.html`
    - Complete API specs at `/v3/api-docs`
    - Request/response examples
- **Code Quality**
    - MapStruct for clean DTO mapping
    - Jakarta Validation for request validation
    - Comprehensive test coverage (JUnit 5, Mockito)



### 🔐 Security & Authentication

- **JWT-based Authentication**
    - Token delivery via `Authorization: Bearer <token>` header OR HttpOnly cookie
    - BCrypt password hashing
    - Stateless sessions for horizontal scalability
    - Custom JWT authentication filter

- **Role-Based Access Control (RBAC)**
    - `ROLE_USER` - Standard trading operations
    - Method-level security with `@PreAuthorize`
    - First registered user automatically receives admin role

- **Security Best Practices**
    - CSRF protection (disabled for stateless API)
    - Centralized exception handling
    - Input validation at all entry points


---

## 🛠 Tech Stack

<table>
<tr>
<td width="50%" valign="top">

### Backend
- **Framework:** Spring Boot 3.5.8
- **Language:** Java 17
- **Security:** Spring Security + JWT (JJWT 0.12.6)
- **Database:** H2 (dev)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Containerization:** Google Jib 3.4.0
- **API Documentation:** Springdoc OpenAPI 2.x
- **Object Mapping:** MapStruct 1.6.3
- **Testing:** JUnit 5, Mockito, Spring Security Test
- **Monitoring:** Spring Boot Actuator

</td>
<td width="50%" valign="top">

### Frontend
- **Framework:** Next.js 16.0.3
- **Language:** TypeScript 5.0
- **UI Library:** React 18
- **Styling:** Tailwind CSS
- **Components:** Radix UI, Shadcn/ui
- **State Management:** React Context API, TanStack Query
- **Form Handling:** React Hook Form
- **Validation:** Zod
- **Charts:** Recharts
- **HTTP Client:** Axios
- **Notifications:** Sonner
- **Icons:** Lucide React, Heroicons
- **Containerization:** Docker (Multi-stage)

</td>
</tr>
</table>

### DevOps & Tools
- **Containerization:** Docker Compose orchestration
- **Backend Build:** Google Jib (daemon-less containerization)
- **Frontend Build:** Dockerfile (optimized multi-stage build)
- **Version Control:** Git
- **API Testing:** Postman
---

## 🗄️ Data Model

### JPA Entities

```
┌──────────────┐         ┌──────────────────┐         ┌──────────────┐
│    Stock     │         │  StockListing    │         │StockExchange │
├──────────────┤         ├──────────────────┤         ├──────────────┤
│ stockId (PK) │────────>│ stockId (FK)     │<────────│exchangeId(PK)│
│ name         │         │ exchangeId (FK)  │         │ name         │
│ description  │         │ [composite PK]   │         │ description  │
│ currentPrice │         └──────────────────┘         │ liveInMarket │
│ updatedAt    │                                       │ version      │
│ version      │                                       └──────────────┘
└──────────────┘

┌──────────────┐
│     User     │
├──────────────┤
│ userId (PK)  │
│ username     │
│ email        │
│ password     │
│ authorities  │
│ createdAt    │
│ updatedAt    │
│ createdBy    │
│ updatedBy    │
│ version      │
└──────────────┘
```

### Entity Details

**Stock**
- Primary Key: `stockId`
- Optimistic locking: `version`
- Fields: name, description, currentPrice, updatedAt
- Relationships: One-to-many `stockListings`

**StockExchange**
- Primary Key: `stockExchangeId`
- Optimistic locking: `version`
- Fields: name, description, liveInMarket (boolean)
- Relationships: One-to-many `stockListings`

**StockListing** (Junction Table)
- Composite Primary Key: `StockListingId` (exchangeId + stockId)
- Links stocks to exchanges (many-to-many)

**User**
- Primary Key: `userId`
- Auditing: createdAt, updatedAt, createdBy, updatedBy
- Optimistic locking: `version`
- Authorities stored via `@ElementCollection`
- Password hashed with BCrypt

### Database Configuration

**Development:** H2 file database
- Location: `./data/stockexchangedb`
- JDBC URL: `jdbc:h2:file:./data/stockexchangedb`
- Bootstrap: `schema.sql` and `data.sql` in resources

---


## 🚀 Quick Start

### Prerequisites

Ensure you have the following installed:

- **Java 17+** ([Download](https://adoptium.net/))
- **Node.js 18+** ([Download](https://nodejs.org/))
- **Maven 3.8+** (or use included wrapper)
- **Docker & Docker Compose** (Optional but recommended, [Download](https://www.docker.com/))

### Option 1: Docker Compose (Recommended) 🐳

The fastest way to get started. Backend builds with **Google Jib** (no Docker daemon needed for build), frontend uses **multi-stage Dockerfile**:

```bash
# Clone the repository
git clone https://github.com/Abdullah-Ebrahim-Othman/Stock-Exchange-Case.git
cd Stock-Exchange-Case

cd backend
mvn clean package
mvn jib:dockerBuild

# Build and start all services

# Jib builds backend image, Dockerfile builds frontend
cd frontend
npm install # install node packages


# build then run the containers to start all services
docker-compose up --build

# Now everything should be working

```

**Access the application:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api/v1
- API Documentation: http://localhost:8080/swagger-ui.html

**Manage services:**
```bash
# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild specific service
docker-compose up --build backend
```


**Backend Environment Variables:**

```properties
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# Database (H2 for dev)
SPRING_DATASOURCE_URL=jdbc:h2:file:./data/stockexchangedb
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver

# JWT Configuration
JWT_SECRET=your-base64-encoded-secret-key-min-256-bits
JWT_EXPIRATION=86400000

# Server Configuration
SERVER_PORT=8080

# CORS
CORS_ALLOWED_ORIGINS=http://localhost:3000
```


**Frontend Environment Variables:**

```bash
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```
---
## 📚 API Documentation

Base URL: `/api/v1`

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| POST | `/auth/register` | Register new user | No | - |
| POST | `/auth/login` | User login (sets HttpOnly cookie) | No | - |
| POST | `/auth/logout` | Logout and clear cookie | Yes | USER |


### Stock Endpoints

| Method | Endpoint | Description | Auth Required | Role |
|--------|----------|-------------|---------------|------|
| GET | `/stock` | List all stocks (paginated) | Yes | USER |
| GET | `/stock/{id}` | Get stock details | Yes | USER |
| GET | `/stock/stocks/{stockId}/exchanges` | List exchanges for stock | Yes | USER |
| POST | `/stock` | Create new stock | Yes | ADMIN |
| PUT | `/stock/{id}/price` | Update stock price | Yes | ADMIN |
| DELETE | `/stock/{id}` | Delete stock | Yes | ADMIN |

**Query Parameters for `/stock`:**
- `page` (default: 0)
- `size` (default: 10)
- `sort` (default: stockId)
- `direction` (ASC/DESC, default: ASC)

### Interactive Documentation

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

---

## 🔒 Security Configuration

### Public Endpoints (No Authentication Required)

- `/api/v1/auth/login`
- `/api/v1/auth/register`
- `/swagger-ui/**`
- `/v3/api-docs/**`

### Protected Endpoints

All other endpoints require authentication via either:

1. **HTTP Header:**
   ```
   Authorization: Bearer <jwt-token>
   ```

2. **HttpOnly Cookie:**
   ```
   Cookie: jwt=<jwt-token>
   ```

### JWT Configuration

- **Algorithm:** HS256
- **Expiration:** 4 hours (configurable)
- **Claims:** userId, username, authorities
- **Secret:** Base64-encoded, minimum 256 bits

### CORS Configuration (Development)

Allowed origins:
- `http://localhost:3000` (Next.js dev server)

For production, update CORS configuration in `SecurityConfig.java`.

### Role Hierarchy

- **ROLE_USER** - Standard operations (view stocks, exchanges, make trades)
- **ROLE_ADMIN** - All USER permissions + management operations (create/update/delete)

First registered user automatically receives `ROLE_ADMIN`.

---

## 🏗 Architecture


### Backend Architecture (Layered)

```
┌─────────────────────────────────────────────┐
│         Controller Layer                    │
│  (REST APIs, @RestController)               │
│  - Request validation                       │
│  - Response formatting                      │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         Service Layer                       │
│  (Business Logic, @Service)                 │
│  - Transaction management                   │
│  - Authorization checks                     │
│  - DTO mapping (MapStruct)                  │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         Repository Layer                    │
│  (Data Access, JpaRepository)               │
│  - Custom queries                           │
│  - Pagination support                       │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│         Database Layer                      │
│  (H2 dev )                                  │
│  - ACID compliance                          │
│  - Optimistic locking                       │
└─────────────────────────────────────────────┘
```

### Security Flow

```
         User Request
              │
              ▼
   ┌──────────────────────┐
   │ JwtAuthentication    │
   │ Filter               │
   │ - Extract token      │
   │ - Validate signature │
   └──────────┬───────────┘
              │
              ▼
   ┌──────────────────────┐      ┌─────────────────┐
   │ Valid Token?         │─NO──>│ 401 Unauthorized│
   └──────────┬───────────┘      └─────────────────┘
              │ YES
              ▼
   ┌──────────────────────┐
   │ Load User Details    │
   │ Set SecurityContext  │
   └──────────┬───────────┘
              │
              ▼
   ┌──────────────────────┐
   │ @PreAuthorize Check  │
   │ Role verification    │
   └──────────┬───────────┘
              │
              ▼
   ┌──────────────────────┐
   │ Controller Method    │
   │ Business Logic       │
   └──────────────────────┘
```

### Containerization Strategy

**Backend: Google Jib**
- Builds OCI images without Docker daemon
- Layer optimization for faster builds
- Reproducible builds
- Direct registry push

**Frontend: Multi-stage Dockerfile**
- Stage 1: Dependencies installation
- Stage 2: Build Next.js app
- Stage 3: Production runtime
- Alpine-based for minimal size

---

## 🧪 Testing

### Backend Testing

The project includes comprehensive tests covering:
- Service layer logic
- JWT lifecycle
- Authentication flows
- Repository operations

**Run Tests:**
```bash
cd backend

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=StockServiceTest

# Run with coverage
./mvnw test jacoco:report

# Integration tests
./mvnw verify

# Skip tests during build
./mvnw clean package -DskipTests
```

**Test Categories:**

1. **Service Tests**
    - `StockServiceTest` - Stock CRUD operations
    - `StockExchangeServiceTest` - Exchange management
    - `JwtServiceTest` - Token generation and validation

2. **Controller Tests**
    - `AuthControllerTest` - Login/register flows
    - Integration with Spring Security Test

3. **Repository Tests**
    - Custom query validation
    - Pagination testing

**Test Configuration:**
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Test Plan Reference

Comprehensive test scenarios covering:
- User registration and authentication
- Stock creation and price updates
- Exchange management
- Stock listing operations
- Pagination and sorting
- Role-based authorization
- JWT token lifecycle
- Error handling and validation

---

## 🔧 Configuration

### Spring Profiles

**Development (`application-dev.yml`)**
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/stockexchangedb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update

logging:
  level:
    root: INFO
    com.example.stockexchange: DEBUG
```


## 👥 Author

**Abdullah Ebrahim Othman**
- Email: abdullah.othmansaleh@gmail.com
- GitHub: [@Abdullah-Ebrahim-Othman](https://github.com/Abdullah-Ebrahim-Othman)
- LinkedIn: [Connect with me](https://www.linkedin.com/in/abdullah-othman-saleh-7b0304240/)

---


**Made with ❤️ by [Abdullah Ebrahim Othman](https://github.com/Abdullah-Ebrahim-Othman)**