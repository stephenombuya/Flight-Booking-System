# ✈️ Flight Booking System

A production-ready Spring Boot backend for searching, booking, and managing flight reservations. Built with clean architecture, JWT security, and comprehensive test coverage.

---

## 📋 Table of Contents
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Security](#security)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (JJWT 0.11) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 (H2 for tests) |
| Documentation | SpringDoc OpenAPI 3 (Swagger UI) |
| Build | Maven |
| Code Coverage | JaCoCo |

---

## 🏛 Architecture

The project uses a classic **N-Tier Layered Architecture**:

```
HTTP Request
    │
    ▼
┌─────────────────────────┐
│   Controller Layer       │  REST endpoints, request validation
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│   Service Layer          │  Business logic, transactions
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│   Repository Layer       │  Spring Data JPA, JPQL queries
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│   Database (MySQL)       │
└─────────────────────────┘
```

Cross-cutting concerns handled separately:
- **Security Layer** — JWT filter, Spring Security config
- **Exception Layer** — `GlobalExceptionHandler` for consistent error responses
- **DTO Layer** — Strict separation between API contracts and domain entities

---

## ✨ Features

### User Management
- Registration and login with JWT token issuance
- Role-based access: `CUSTOMER` and `ADMIN`
- Profile view and update (owner or admin only)
- Secure password change with BCrypt hashing

### Flight Management
- Full CRUD for admins
- Public flight search with filters: origin, destination, date range, price range, availability, status
- Paginated and sortable results
- Automatic seat count tracking

### Booking Management
- Create, view, confirm, and cancel bookings
- Atomic seat reservation within a transaction
- Human-readable booking references (e.g. `FBS-20240315-00042`)
- Ownership-enforced access (customers can only see their own bookings)

### Payment Processing
- Simulated payment gateway (replace `simulateGatewayCall()` with Stripe/PayPal)
- FAIL_ token prefix triggers a declined payment (for testing)
- Automatic booking confirmation on successful payment
- Refund support

### Admin Dashboard
- System-wide statistics: users, flights, bookings, revenue
- Revenue reports by date range
- User disabling, booking overrides, payment refunds

### Notifications
- Async email on booking confirmation, cancellation, and payment receipt

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.8+

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/stephenombuya/Flight-Booking-System
   cd flight-booking-system
   ```

2. **Create the database**
   ```sql
   CREATE DATABASE flight_booking_db;
   ```

3. **Configure `application.properties`**
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/flight_booking_db
   spring.datasource.username=root
   spring.datasource.password=yourpassword

   # Generate a secure 256-bit base64 secret:
   # openssl rand -base64 32
   app.jwt.secret=YOUR_BASE64_SECRET_HERE
   app.jwt.expiration-ms=86400000

   spring.mail.username=your@email.com
   spring.mail.password=your_app_password
   ```

4. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Access Swagger UI**
   ```
   http://localhost:8080/swagger-ui/index.html
   ```

---

## 📖 API Reference

### Authentication
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new customer | Public |
| POST | `/api/v1/auth/login` | Login, receive JWT | Public |

### Flights
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/flights` | Search flights (with filters) | Public |
| GET | `/api/v1/flights/{id}` | Get flight details | Public |
| POST | `/api/v1/flights` | Create a flight | Admin |
| PUT | `/api/v1/flights/{id}` | Update a flight | Admin |
| DELETE | `/api/v1/flights/{id}` | Delete a flight | Admin |

**Search Query Parameters:** `origin`, `destination`, `fromDate`, `toDate`, `minPrice`, `maxPrice`, `status`, `minSeats`, `page`, `size`, `sort`

### Bookings
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/bookings` | Create a booking | Customer |
| GET | `/api/v1/bookings/{id}` | Get booking by ID | Owner/Admin |
| GET | `/api/v1/bookings/reference/{ref}` | Get by reference | Owner/Admin |
| GET | `/api/v1/bookings/my` | List my bookings | Customer |
| DELETE | `/api/v1/bookings/{id}/cancel` | Cancel a booking | Owner/Admin |

### Payments
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/payments` | Process payment | Customer |
| GET | `/api/v1/payments/booking/{bookingId}` | Get payment details | Owner/Admin |

### Admin
| Method | Endpoint | Description | Auth |
|---|---|---|---|
| GET | `/api/v1/admin/dashboard` | Dashboard stats | Admin |
| GET | `/api/v1/admin/revenue` | Revenue by date range | Admin |
| GET | `/api/v1/admin/users` | List all users | Admin |
| PATCH | `/api/v1/admin/users/{id}/disable` | Disable user | Admin |
| GET | `/api/v1/admin/bookings` | List all bookings | Admin |
| PATCH | `/api/v1/admin/bookings/{id}/confirm` | Confirm booking | Admin |
| PATCH | `/api/v1/admin/bookings/{id}/cancel` | Cancel any booking | Admin |
| PATCH | `/api/v1/admin/payments/{id}/refund` | Refund payment | Admin |

### Error Response Format
```json
{
  "timestamp": "2024-03-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Flight not found with id: '99'",
  "path": "/api/v1/flights/99"
}
```

Validation errors include a `fieldErrors` map:
```json
{
  "status": 422,
  "error": "Validation Failed",
  "fieldErrors": {
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## 🔐 Security

- **Stateless JWT** — No server-side sessions. Each request carries a signed Bearer token.
- **BCrypt** — All passwords are hashed with strength factor 10 before storage.
- **Role Enforcement** — Spring Security `@PreAuthorize` + HTTP security rules. Admin routes are double-protected.
- **CORS** — Configurable in `SecurityConfig.corsConfigurationSource()`.
- **Input Validation** — All request DTOs use Jakarta Bean Validation (`@Valid`).

**To authenticate in Swagger UI:**
1. Call `POST /api/v1/auth/login` → copy `accessToken`
2. Click **Authorize** → paste token in the `bearerAuth` field

---

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage report
mvn verify

# View coverage report
open target/site/jacoco/index.html
```

### Test Strategy

| Type | Class | Scope |
|---|---|---|
| Unit | `AuthServiceTest` | Auth logic, token generation |
| Unit | `FlightServiceTest` | CRUD, validation, seat management |
| Unit | `BookingServiceTest` | Lifecycle, authorization, seat reservation |
| Unit | `PaymentServiceTest` | Gateway simulation, refunds |
| Unit | `UserServiceTest` | Profile updates, password change |
| Controller | `AuthControllerTest` | HTTP status, validation errors |
| Controller | `FlightControllerTest` | RBAC, request/response mapping |
| Repository | `FlightRepositoryTest` | JPQL search query with all filter combinations |
| Repository | `BookingRepositoryTest` | Revenue calculation, status counts |
| Integration | `FlightBookingApplicationTests` | Full context load |

---

## 📁 Project Structure

```
src/
├── main/java/com/flightbookingapp/
│   ├── FlightBookingApplication.java     # Entry point
│   ├── controller/                       # REST controllers
│   │   ├── AuthController.java
│   │   ├── FlightController.java
│   │   ├── BookingController.java
│   │   ├── PaymentController.java
│   │   ├── UserController.java
│   │   └── AdminController.java
│   ├── service/                          # Interfaces
│   │   └── impl/                         # Implementations
│   ├── repository/                       # Spring Data JPA repositories
│   ├── model/                            # JPA entities + enums
│   ├── dto/
│   │   ├── request/                      # Validated input DTOs
│   │   └── response/                     # Output DTOs
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── ApplicationConfig.java
│   │   └── OpenApiConfig.java
│   ├── security/
│   │   ├── JwtService.java
│   │   └── JwtAuthenticationFilter.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   └── [Custom exceptions]
│   └── util/
│       ├── BookingReferenceGenerator.java
│       ├── EmailService.java
│       └── PageUtils.java
├── main/resources/
│   └── application.properties
└── test/java/com/flightbookingapp/
    ├── TestDataFactory.java
    ├── controller/
    ├── service/
    └── repository/
```

---

## 📄 License

This project is licensed under the **MIT License**.
