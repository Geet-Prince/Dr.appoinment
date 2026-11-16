# 🩺 Dr. Appointment - AI-Powered Doctor Appointment & Patient Management System

A robust, modern healthcare backend application built with **Spring Boot 4**, **Spring AI**, **Google GenAI (Gemini)**, and **PostgreSQL**. The system provides seamless patient management and is architected to support intelligent doctor appointment scheduling and AI-assisted healthcare workflows.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Key Features (Till Now)](#-key-features-till-now)
- [Tech Stack](#-tech-stack)
- [System Architecture](#-system-architecture)
- [Project Structure](#-project-structure)
- [API Endpoints Reference](#-api-endpoints-reference)
  - [Patient Management](#patient-endpoints-apipatients)
    - [1. Register Patient](#1-register-patient)
    - [2. Get All Patients](#2-get-all-patients)
    - [3. Get Patient by ID](#3-get-patient-by-id)
    - [4. Update Patient](#4-update-patient)
    - [5. Delete Patient](#5-delete-patient)
  - [Department Management](#department-endpoints-apidepartments)
    - [6. Create Department](#6-create-department)
    - [7. Get All Departments](#7-get-all-departments)
    - [8. Search Departments](#8-search-departments)
    - [9. Get Department by ID](#9-get-department-by-id)
    - [10. Update Department](#10-update-department)
    - [11. Delete Department](#11-delete-department)
- [Validation Rules](#-validation-rules)
- [Error Handling](#-error-handling)
- [Configuration & Setup](#-configuration--setup)
  - [Prerequisites](#prerequisites)
  - [Database Configuration](#database-configuration)
  - [Application Properties](#application-properties)
  - [Running the Application](#running-the-application)
- [Upcoming Roadmap](#-upcoming-roadmap)

---

## 🌟 Overview

The **Dr. Appointment** system provides a clean, modular, and scalable RESTful API foundation for managing patients, doctor appointments, and medical interactions. Integrated with **Spring AI** and **Google Gemini**, the project is designed to combine traditional clinical management with AI-driven capabilities such as automated symptom triage, intelligent appointment matching, and patient inquiry resolution.

---

## 🚀 Key Features (Till Now)

- **Comprehensive Patient Management (CRUD):**
  - Register new patients with unique email enforcement.
  - Retrieve patient listings or single patient records by ID.
  - Update patient profiles with conflict checks.
  - Delete patient records with proper existence verification.
- **Comprehensive Department Management (CRUD + Search):**
  - Register clinical departments (Cardiology, Neurology, Pediatrics, etc.) with case-insensitive uniqueness.
  - Search departments by name or keyword.
  - Update department details with collision checks.
  - Delete departments with existence verification.
- **Robust Input Validation:**
  - Strict validation using Jakarta Validation (`@NotBlank`, `@Email`, `@Pattern`, `@Min`, `@Max`, `@NotNull`).
- **Centralized Global Exception Handling:**
  - Unified error responses for `404 Not Found` (ResourceNotFoundException), `409 Conflict` (DuplicateResourceException), and input validation errors.
- **Layered Architecture:**
  - Clean separation of concerns across Controllers, Services, Repositories, DTOs, and Entities.
- **Spring AI & Google Gemini Integration Setup:**
  - Configured with `spring-ai-starter-model-google-genai` and Spring AI 2.0.1 BOM for upcoming generative AI capabilities.
- **Database & Persistence:**
  - PostgreSQL integration with Hibernate auto-schema management (`ddl-auto=update`) and formatted SQL logging.
- **Spring Security Baseline:**
  - Configured with CSRF disabled and open endpoint access for RESTful API testing and development.

---

## 🛠️ Tech Stack

| Component | Technology | Version |
| :--- | :--- | :--- |
| **Language** | Java | 27 |
| **Framework** | Spring Boot | 4.1.2 |
| **AI Framework** | Spring AI (Google GenAI Starter) | 2.0.1 |
| **Persistence** | Spring Data JPA / Hibernate | 4.1.2 |
| **Database** | PostgreSQL | 14+ / Latest |
| **Security** | Spring Security | 4.1.2 |
| **Validation** | Jakarta Validation (Hibernate Validator) | Included |
| **Boilerplate Reduction** | Project Lombok | Latest |
| **Build Tool** | Apache Maven Wrapper | 3.9+ |

---

## 🏛️ System Architecture

The project follows a standard multi-tier enterprise Spring Boot architecture:

```
[ Client / Frontend / Postman ]
               |
               v
    [ Spring Security Filter ]
               |
               v
     [ PatientController ]  <---> [ DTOs: PatientRequest / PatientResponse ]
               |
               v
       [ PatientService ]   <---> [ GlobalExceptionHandler ]
               |
               v
     [ PatientRepository ]
               |
               v
     [ PostgreSQL Database ]
```

---

## 📁 Project Structure

```
Dr.appoinment/
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── src/
│   ├── main/
│   │   ├── java/me/geetprince/Dr/appoinment/
│   │   │   ├── Application.java                 # Main Spring Boot Application Entry Point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java          # Security & CORS/CSRF Configuration
│   │   │   ├── controller/
│   │   │   │   └── PatientController.java       # REST Controller for /api/patients
│   │   │   ├── dto/
│   │   │   │   ├── PatientRequest.java          # Validated Request Payload DTO
│   │   │   │   └── PatientResponse.java         # API Response Data DTO
│   │   │   ├── exception/
│   │   │   │   ├── DuplicateResourceException.java  # Custom 409 Conflict Exception
│   │   │   │   ├── GlobalExceptionHandler.java      # Centralized @RestControllerAdvice
│   │   │   │   └── ResourceNotFoundException.java   # Custom 404 Not Found Exception
│   │   │   ├── models/
│   │   │   │   └── Patient.java                 # JPA Entity for "patients" table
│   │   │   ├── repository/
│   │   │   │   └── PatientRepository.java       # Spring Data JPA Repository
│   │   │   └── service/
│   │   │       └── PatientService.java          # Business Logic & Entity-to-DTO Mapping
│   │   └── resources/
│   │       └── application.properties           # PostgreSQL, JPA & Spring AI Configuration
│   └── test/
│       └── java/me/geetprince/Dr/appoinment/
│           └── ApplicationTests.java            # Spring Boot Context Tests
├── mvnw                                         # Unix Maven Wrapper
├── mvnw.cmd                                     # Windows Maven Wrapper
├── pom.xml                                      # Maven Dependencies & Plugins
└── README.md                                    # Project Documentation
```

---

## 📡 API Endpoints Reference

### Patient Endpoints (`/api/patients`)

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/patients` | Register a new patient | `201 Created` |
| `GET` | `/api/patients` | Retrieve all registered patients | `200 OK` |
| `GET` | `/api/patients/{id}` | Get details of a patient by ID | `200 OK` |
| `PUT` | `/api/patients/{id}` | Update an existing patient's details | `200 OK` |
| `DELETE` | `/api/patients/{id}` | Delete a patient by ID | `204 No Content` |

### Department Endpoints (`/api/departments`)

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/departments` | Create a new department | `201 Created` |
| `GET` | `/api/departments` | Retrieve all departments | `200 OK` |
| `GET` | `/api/departments/search` | Search departments by name keyword (`?name=...`) | `200 OK` |
| `GET` | `/api/departments/{id}` | Get department by ID | `200 OK` |
| `PUT` | `/api/departments/{id}` | Update department details | `200 OK` |
| `DELETE` | `/api/departments/{id}` | Delete a department by ID | `204 No Content` |

---

### 1. Register Patient

Creates a new patient profile. Checks for duplicate email registration.

- **URL:** `POST /api/patients`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "fullName": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "9876543210",
  "age": 28,
  "gender": "Female"
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "fullName": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "9876543210",
  "age": 28,
  "gender": "Female"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Doe",
    "email": "jane.doe@example.com",
    "phoneNumber": "9876543210",
    "age": 28,
    "gender": "Female"
  }'
```

---

### 2. Get All Patients

Fetches all patients registered in the database.

- **URL:** `GET /api/patients`
- **Headers:** `Accept: application/json`

#### Success Response (`200 OK`)
```json
[
  {
    "id": 1,
    "fullName": "Jane Doe",
    "email": "jane.doe@example.com",
    "phoneNumber": "9876543210",
    "age": 28,
    "gender": "Female"
  }
]
```

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/patients
```

---

### 3. Get Patient by ID

Retrieves single patient record matching the provided ID.

- **URL:** `GET /api/patients/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### Success Response (`200 OK`)
```json
{
  "id": 1,
  "fullName": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "9876543210",
  "age": 28,
  "gender": "Female"
}
```

#### Error Response (`404 Not Found`)
```json
{
  "timestamp": "2026-11-16T13:45:10.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with ID: 99"
}
```

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/patients/1
```

---

### 4. Update Patient

Updates patient details. Prevents updating the email to an address already registered by another patient.

- **URL:** `PUT /api/patients/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "phoneNumber": "9876543210",
  "age": 29,
  "gender": "Female"
}
```

#### Success Response (`200 OK`)
```json
{
  "id": 1,
  "fullName": "Jane Smith",
  "email": "jane.smith@example.com",
  "phoneNumber": "9876543210",
  "age": 29,
  "gender": "Female"
}
```

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/patients/1 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Jane Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "9876543210",
    "age": 29,
    "gender": "Female"
  }'
```

---

### 5. Delete Patient

Removes a patient record permanently from the database.

- **URL:** `DELETE /api/patients/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### Success Response (`204 No Content`)
*Empty response body.*

#### Error Response (`404 Not Found`)
```json
{
  "timestamp": "2026-11-16T13:45:10.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with ID: 99"
}
```

#### cURL Example
```bash
curl -X DELETE http://localhost:8080/api/patients/1
```

---

### 6. Create Department

Registers a new clinical department (e.g., Cardiology, Neurology). Enforces case-insensitive unique naming.

- **URL:** `POST /api/departments`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "name": "Cardiology",
  "description": "Comprehensive cardiac, vascular, and thoracic clinical care."
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "name": "Cardiology",
  "description": "Comprehensive cardiac, vascular, and thoracic clinical care.",
  "createdAt": "2026-11-16T14:00:00"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/departments \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Cardiology",
    "description": "Comprehensive cardiac, vascular, and thoracic clinical care."
  }'
```

---

### 7. Get All Departments

Retrieves all configured clinical departments.

- **URL:** `GET /api/departments`
- **Headers:** `Accept: application/json`

#### Success Response (`200 OK`)
```json
[
  {
    "id": 1,
    "name": "Cardiology",
    "description": "Comprehensive cardiac, vascular, and thoracic clinical care.",
    "createdAt": "2026-11-16T14:00:00"
  }
]
```

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/departments
```

---

### 8. Search Departments

Searches departments by name or partial keyword.

- **URL:** `GET /api/departments/search?name={keyword}`
- **Query Parameter:** `name` (optional, string)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/departments/search?name=cardio"
```

---

### 9. Get Department by ID

Retrieves details of a department by its ID.

- **URL:** `GET /api/departments/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/departments/1
```

---

### 10. Update Department

Updates an existing department's name and description. Prevents name conflicts with other departments.

- **URL:** `PUT /api/departments/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "name": "Advanced Cardiology",
  "description": "Advanced cardiology and cardiovascular surgical care."
}
```

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/departments/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Advanced Cardiology",
    "description": "Advanced cardiology and cardiovascular surgical care."
  }'
```

---

### 11. Delete Department

Deletes a clinical department by ID.

- **URL:** `DELETE /api/departments/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X DELETE http://localhost:8080/api/departments/1
```

---

## 🔒 Validation Rules

The application uses Jakarta Validation annotations on `PatientRequest` and `DepartmentRequest`:

### Patient Validation (`PatientRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `fullName` | `@NotBlank` | `"Name is required"` |
| `email` | `@NotBlank`, `@Email` | `"Email is required"`, `"Invalid email address"` |
| `phoneNumber` | `@NotBlank`, `@Pattern(regexp = "^[0-9]{10,15}$")` | `"Phone number is required"`, `"Phone number must contain 10 to 15 digits"` |
| `age` | `@NotNull`, `@Min(1)`, `@Max(120)` | `"Age is required"`, `"Age must be at least 1"`, `"Age cannot exceed 120"` |
| `gender` | `@NotBlank` | `"Gender is required"` |

### Department Validation (`DepartmentRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `name` | `@NotBlank`, `@Size(min = 2, max = 100)` | `"Department name is required"`, `"Department name must be between 2 and 100 characters"` |
| `description` | `@Size(max = 1000)` | `"Description must not exceed 1000 characters"` |

---

## ⚠️ Error Handling

All standard exceptions are captured by `@RestControllerAdvice` in `GlobalExceptionHandler`:

### 1. Duplicate Resource (`409 Conflict`)
Triggered when registering or updating a patient with an existing email:
```json
{
  "timestamp": "2026-11-16T13:45:10.123456",
  "status": 409,
  "error": "Conflict",
  "message": "Email is already registered"
}
```

### 2. Resource Not Found (`404 Not Found`)
Triggered when requesting or modifying a non-existent patient ID:
```json
{
  "timestamp": "2026-11-16T13:45:10.123456",
  "status": 404,
  "error": "Not Found",
  "message": "Patient not found with ID: 42"
}
```

---

## ⚙️ Configuration & Setup

### Prerequisites

- **Java Development Kit (JDK):** Version 27 (or compatible 21+ with bytecode adjustment)
- **Database:** PostgreSQL (running locally or remotely)
- **Maven:** 3.9+ (or use the included `./mvnw`)

---

### Database Configuration

1. Start your PostgreSQL service.
2. Create the application database:
   ```sql
   CREATE DATABASE dr_appointment;
   ```

---

### Application Properties

The application configuration is managed in `src/main/resources/application.properties`:

```properties
spring.application.name=Dr.appoinment

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/dr_appointment
spring.datasource.username=${DB_USERNAME:geet}
spring.datasource.password=${DB_PASSWORD:0}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Gemini / Spring AI Configuration
spring.ai.google.genai.api-key=${GEMINI_API_KEY:your_api_key_here}
```

> **Tip:** You can supply your Gemini API key via the `GEMINI_API_KEY` environment variable:
> ```bash
> export GEMINI_API_KEY="your_actual_gemini_api_key"
> ```

---

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Geet-Prince/Dr.appoinment.git
   cd Dr.appoinment
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean compile
   ```

3. **Run the Spring Boot application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. The server will start on port `8080` (default: `http://localhost:8080`).

---

## 🗺️ Implementation Progress & Roadmap

- [x] **Phase 1 — Patient Management:** CRUD, unique constraints, DTO separation, validation.
- [x] **Phase 2 — Department Management:** CRUD, unique name check, fuzzy search, unit & MockMvc test suites.
- [ ] **Phase 3 — Doctor Management:** Doctor profiles, department relationships, pagination, multi-criteria filtering.
- [ ] **Phase 4 — Doctor Availability:** Weekly consultation schedules, slots generation, unavailable day flags.
- [ ] **Phase 5 — Appointment Scheduling Engine:** Concurrency-safe slot booking, rescheduling, cancellations.
- [ ] **Phase 6 — Prescription Management:** Post-consultation diagnoses, medicines, instructions.
- [ ] **Phase 7 — Review & Rating:** Verified reviews for completed appointments, rating aggregation.
- [ ] **Phase 8 — Authentication & Authorization:** Spring Security, BCrypt, JWT, Role-Based Access Control (RBAC).
- [ ] **Phase 9 — AI Healthcare Assistant (Google Gemini):** Spring AI ChatClient, tools calling, session isolation.
- [ ] **Phase 10 — Notifications & Enhancements:** Automated email/SMS reminders, Dockerization.

---

## 👨‍💻 Author

- **Prince Raj** ([@Geet-Prince](https://github.com/Geet-Prince))
- Website: [https://Geetprince.me](https://Geetprince.me)
