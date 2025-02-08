# Flight Booking System

## Overview
The Flight Booking System is a back-end application designed for flight reservations. It allows users to search, book, and manage their flight reservations while providing administrators with tools to manage availability and payments.

## Technologies Used
- **Java**: Main programming language.
- **Spring Boot**: Framework for building RESTful APIs.
- **MySQL**: Database for storing bookings, users, and payment transactions.
- **Spring Data JPA**: ORM for interacting with the database.
- **Spring Security**: For user authentication and authorization.
- **Swagger/OpenAPI**: For API documentation.

## System Structure
### **1. Architecture Pattern**
- **Layered Architecture** (N-tier architecture)
- Main layers:
  - **Controller Layer**: Handles HTTP requests and responses.
  - **Service Layer**: Implements business logic.
  - **Repository Layer**: Interacts with the database using Spring Data JPA.
  - **Security Layer**: Manages authentication and authorization.

### **2. Package Structure**
```
com.flightbookingapp
│── controller      # Handles HTTP requests
│── service         # Contains business logic
│── repository      # Database access
│── model           # Entity classes representing database tables
│── dto             # Data Transfer Objects for API requests/responses
│── config          # Configuration files (Security, Database, etc.)
│── exception       # Custom exception handling
│── util            # Utility classes (e.g., email service, validators)
```

### **3. Database Schema**
- **Users**: Stores customer and admin details (ID, name, email, role, etc.).
- **Bookings**: Tracks flight reservations (ID, user, flight details, status, payment info).
- **Flights**: Stores flight details, schedules, and seat availability.
- **Payments**: Manages transactions for reservations.

## Features
### 1. User Management
- Secure login and account management for customers and admins.
- Role-based access control.

### 2. Search & Filtering
- Users can search for flights based on destination, date, and price range.
- Advanced filters for refining search results.

### 3. Booking Management
- Users can create, view, update, or cancel flight reservations.
- Real-time availability checking for flights.

### 4. Payment Integration
- Secure online payments for bookings.
- Integration with payment gateways for transaction processing.

### 5. Admin Dashboard
- Manage bookings, monitor availability, and generate financial reports.
- View system usage and user activity analytics.

## Installation & Setup
### Prerequisites
- Java 17+
- MySQL Server
- Maven

### Steps to Run
1. Clone the repository:
   ```sh
   git clone https://github.com/stephenombuya/Flight-Booking-System
   cd flight-booking-system
   ```
2. Configure MySQL database in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/flight_booking_db
   spring.datasource.username=root
   spring.datasource.password=yourpassword
   ```
3. Install dependencies:
   ```sh
   mvn clean install
   ```
4. Run the application:
   ```sh
   mvn spring-boot:run
   ```

## API Documentation
After starting the application, access API docs at:
```
http://localhost:8080/swagger-ui/index.html
```

## Contribution
Contributions are welcome! Feel free to submit a pull request or open an issue.

## License
This project is licensed under the MIT License.

