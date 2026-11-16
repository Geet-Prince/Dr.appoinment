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
  - [Doctor Management](#doctor-endpoints-apidoctors)
    - [12. Register Doctor](#12-register-doctor)
    - [13. Get All Doctors (Paginated)](#13-get-all-doctors-paginated)
    - [14. Get Doctor by ID](#14-get-doctor-by-id)
    - [15. Get Doctors by Department](#15-get-doctors-by-department)
    - [16. Search Doctors (Multi-Criteria)](#16-search-doctors-multi-criteria)
    - [17. Update Doctor](#17-update-doctor)
    - [18. Delete Doctor](#18-delete-doctor)
  - [Doctor Availability & Schedule](#doctor-availability-endpoints-apidoctorsdoctorid)
    - [19. Add Working Schedule](#19-add-working-schedule)
    - [20. Get Doctor Schedule](#20-get-doctor-schedule)
    - [21. Get Available Slots for Date](#21-get-available-slots-for-date)
    - [22. Update Working Schedule](#22-update-working-schedule)
    - [23. Delete Working Schedule](#23-delete-working-schedule)
  - [Appointment Management & Booking](#appointment-endpoints-apiappointments)
    - [24. Book Appointment](#24-book-appointment)
    - [25. Get Appointment by ID](#25-get-appointment-by-id)
    - [26. Get Appointments by Patient](#26-get-appointments-by-patient)
    - [27. Get Appointments by Doctor](#27-get-appointments-by-doctor)
    - [28. Filter Appointments by Status](#28-filter-appointments-by-status)
    - [29. Cancel Appointment](#29-cancel-appointment)
    - [30. Reschedule Appointment](#30-reschedule-appointment)
    - [31. Update Appointment Status](#31-update-appointment-status)
  - [Prescription Management](#prescription-endpoints-apiprescriptions)
    - [32. Create Prescription](#32-create-prescription)
    - [33. Get Prescription by ID](#33-get-prescription-by-id)
    - [34. Get Prescription by Appointment](#34-get-prescription-by-appointment)
    - [35. Get Prescriptions by Patient](#35-get-prescriptions-by-patient)
    - [36. Get Prescriptions by Doctor](#36-get-prescriptions-by-doctor)
    - [37. Update Prescription](#37-update-prescription)
  - [Review & Rating Management](#review--rating-endpoints-apireviews)
    - [38. Submit Review](#38-submit-review)
    - [39. Get Review by ID](#39-get-review-by-id)
    - [40. Get Review by Appointment](#40-get-review-by-appointment)
    - [41. Get Reviews by Doctor](#41-get-reviews-by-doctor)
    - [42. Get Doctor Rating Summary](#42-get-doctor-rating-summary)
    - [43. Get Reviews by Patient](#43-get-reviews-by-patient)
    - [44. Update Review](#44-update-review)
    - [45. Delete Review](#45-delete-review)
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
- **Comprehensive Doctor Management (CRUD, Pagination, Sorting & Multi-Criteria Search):**
  - Register doctors linked to clinical departments with unique email and medical license checks.
  - Paginated doctor listings with Spring Data `Pageable` and configurable sorting.
  - Multi-criteria dynamic filtering by name, specialization, department ID, minimum experience, and maximum consultation fee.
  - Retrieve doctors by department.
  - Full profile updates and cascading-safe deletions.
- **Intelligent Doctor Availability & Dynamic Slot Engine:**
  - Configure recurring weekly shifts per weekday (`DayOfWeek`) with custom start and end times.
  - Granular slot durations (e.g. 15, 30, 45, 60 minutes).
  - Database-level shift overlap prevention preventing double-scheduling on the same weekday.
  - Dynamic slot generator calculating bookable appointment intervals for any calendar date (`LocalDate`), automatically adjusting for past times on the current day.
- **Concurrency-Safe Clinical Appointment Scheduling Engine:**
  - Atomic appointment booking with comprehensive validation pipeline (patient, doctor, schedule alignment, past date/time prevention).
  - Double-booking prevention queries ensuring neither doctor nor patient can have overlapping active bookings (`PENDING` / `CONFIRMED`).
  - Consultation fee snapshotting at time of booking to maintain historical audit accuracy.
  - Full lifecycle workflow: booking, cancellation with reason, rescheduling to open slots, and administrative status transitions (`CONFIRMED`, `COMPLETED`, `REJECTED`, `NO_SHOW`).
  - Paginated queries by patient, doctor, and status with Spring Data `Pageable`.
- **Clinical Prescription Management:**
  - Issue formal digital prescriptions for completed clinical consultations (`COMPLETED` status validation).
  - Enforce one-to-one mapping between consultation appointment and prescription preventing duplicate entries.
  - Snapshot diagnosis, medicine regimen, instructions, and audit timestamp.
  - Paginated prescription history lookup by patient ID and doctor ID.
  - Updates for diagnoses and treatment plans with persistence.
- **Patient Review & Rating System:**
  - Submit 1-to-5 star ratings and clinical consultation reviews for verified `COMPLETED` appointments.
  - Enforce strict one-review-per-appointment constraint preventing spam or artificial inflation.
  - Real-time aggregated doctor rating analytics (average score rounded to 2 decimals, total reviews count).
  - Paginated review listings by doctor and by patient.
  - Update and delete operations for review content.
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
│   │   │   ├── Application.java                             # Main Spring Boot Application Entry Point
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java                      # Security & CORS/CSRF Configuration
│   │   │   ├── controller/
│   │   │   │   ├── AppointmentController.java               # REST Controller for /api/appointments
│   │   │   │   ├── DepartmentController.java                # REST Controller for /api/departments
│   │   │   │   ├── DoctorAvailabilityController.java        # REST Controller for /api/doctors/{id}/availability & slots
│   │   │   │   ├── DoctorController.java                    # REST Controller for /api/doctors
│   │   │   │   └── PatientController.java                   # REST Controller for /api/patients
│   │   │   ├── dto/
│   │   │   │   ├── AppointmentBookingRequest.java           # Validated Appointment Booking DTO
│   │   │   │   ├── AppointmentRescheduleRequest.java        # Validated Appointment Reschedule DTO
│   │   │   │   ├── AppointmentResponse.java                 # Complete Appointment Response DTO
│   │   │   │   ├── AppointmentStatusUpdateRequest.java      # Validated Status Transition DTO
│   │   │   │   ├── DepartmentRequest.java                   # Validated Department Request DTO
│   │   │   │   ├── DepartmentResponse.java                  # Department API Response DTO
│   │   │   │   ├── DoctorAvailabilityRequest.java           # Validated Doctor Shift Schedule Request DTO
│   │   │   │   ├── DoctorAvailabilityResponse.java          # Doctor Shift Schedule Response DTO
│   │   │   │   ├── DoctorRequest.java                       # Validated Doctor Registration/Update DTO
│   │   │   │   ├── DoctorResponse.java                      # Doctor Profile Response DTO
│   │   │   │   ├── PatientRequest.java                      # Validated Patient Request Payload DTO
│   │   │   │   ├── PatientResponse.java                     # Patient API Response Data DTO
│   │   │   │   └── TimeSlotResponse.java                    # Dynamic Consultation Slot Response DTO
│   │   │   ├── exception/
│   │   │   │   ├── AppointmentConflictException.java        # Custom 409 Appointment Conflict Exception
│   │   │   │   ├── DoctorUnavailableException.java          # Custom 400 Doctor Unavailable Exception
│   │   │   │   ├── DuplicateResourceException.java          # Custom 409 Conflict Exception
│   │   │   │   ├── GlobalExceptionHandler.java              # Centralized @RestControllerAdvice
│   │   │   │   ├── InvalidAppointmentException.java         # Custom 400 Invalid Appointment Exception
│   │   │   │   └── ResourceNotFoundException.java           # Custom 404 Not Found Exception
│   │   │   ├── models/
│   │   │   │   ├── Appointment.java                         # JPA Entity for "appointments" table
│   │   │   │   ├── AppointmentStatus.java                   # Enum for appointment lifecycle states
│   │   │   │   ├── Department.java                          # JPA Entity for "departments" table
│   │   │   │   ├── Doctor.java                              # JPA Entity for "doctors" table
│   │   │   │   ├── DoctorAvailability.java                  # JPA Entity for "doctor_availabilities" table
│   │   │   │   └── Patient.java                             # JPA Entity for "patients" table
│   │   │   ├── repository/
│   │   │   │   ├── AppointmentRepository.java               # Spring Data JPA Repository for Appointment
│   │   │   │   ├── DepartmentRepository.java                # Spring Data JPA Repository for Department
│   │   │   │   ├── DoctorAvailabilityRepository.java        # Spring Data JPA Repository for Availability
│   │   │   │   ├── DoctorRepository.java                    # Spring Data JPA Repository for Doctor
│   │   │   │   └── PatientRepository.java                   # Spring Data JPA Repository for Patient
│   │   │   └── service/
│   │   │       ├── AppointmentService.java                  # Appointment Business Engine & Conflict Prevention
│   │   │       ├── DepartmentService.java                   # Department Business Logic
│   │   │       ├── DoctorAvailabilityService.java           # Schedule & Slot Calculation Logic
│   │   │       ├── DoctorService.java                       # Doctor Business Logic & Multi-Criteria Search
│   │   │       └── PatientService.java                      # Patient Business Logic
│   │   └── resources/
│   │       └── application.properties                       # PostgreSQL, JPA & Spring AI Configuration
│   └── test/
│       ├── java/me/geetprince/Dr/appoinment/
│       │   ├── ApplicationTests.java                        # Spring Boot Context Tests
│       │   ├── controller/
│       │   │   ├── AppointmentControllerTest.java           # MockMvc tests for AppointmentController
│       │   │   ├── DepartmentControllerTest.java            # MockMvc tests for DepartmentController
│       │   │   ├── DoctorAvailabilityControllerTest.java    # MockMvc tests for DoctorAvailabilityController
│       │   │   └── DoctorControllerTest.java                # MockMvc tests for DoctorController
│       │   └── service/
│       │       ├── AppointmentServiceTest.java              # Unit tests for AppointmentService
│       │       ├── DepartmentServiceTest.java               # Unit tests for DepartmentService
│       │       ├── DoctorAvailabilityServiceTest.java       # Unit tests for DoctorAvailabilityService
│       │       └── DoctorServiceTest.java                   # Unit tests for DoctorService
│       └── resources/
│           └── mockito-extensions/
│               └── org.mockito.plugins.MockMaker            # Java 27 Mockito Subclass MockMaker
├── mvnw                                                     # Unix Maven Wrapper
├── mvnw.cmd                                                 # Windows Maven Wrapper
├── pom.xml                                                  # Maven Dependencies & Plugins
└── README.md                                                # Project Documentation
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

### Doctor Endpoints (`/api/doctors`)

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/doctors` | Register a new doctor | `201 Created` |
| `GET` | `/api/doctors` | List doctors with pagination and sorting (`?page=0&size=10&sort=id`) | `200 OK` |
| `GET` | `/api/doctors/{id}` | Retrieve doctor profile by ID | `200 OK` |
| `GET` | `/api/doctors/department/{id}` | Retrieve all doctors in a specific department | `200 OK` |
| `GET` | `/api/doctors/search` | Multi-criteria search (`?name=&specialization=&departmentId=&minExperience=&maxFee=`) | `200 OK` |
| `PUT` | `/api/doctors/{id}` | Update doctor profile | `200 OK` |
| `DELETE` | `/api/doctors/{id}` | Delete doctor profile by ID | `204 No Content` |

### Doctor Availability Endpoints (`/api/doctors/{doctorId}`)

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/doctors/{doctorId}/availability` | Add weekly shift schedule | `201 Created` |
| `GET` | `/api/doctors/{doctorId}/availability` | Retrieve doctor's full weekly schedule | `200 OK` |
| `GET` | `/api/doctors/{doctorId}/slots` | Retrieve calculated available slots for date (`?date=YYYY-MM-DD`) | `200 OK` |
| `PUT` | `/api/doctors/{doctorId}/availability/{availabilityId}` | Update schedule shift | `200 OK` |
| `DELETE` | `/api/doctors/{doctorId}/availability/{availabilityId}` | Delete schedule shift | `204 No Content` |

### Appointment Endpoints (`/api/appointments`)

| HTTP Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/appointments` | Book a new appointment | `201 Created` |
| `GET` | `/api/appointments/{id}` | Retrieve appointment by ID | `200 OK` |
| `GET` | `/api/appointments/patient/{patientId}` | Retrieve paginated appointments for a patient | `200 OK` |
| `GET` | `/api/appointments/doctor/{doctorId}` | Retrieve paginated appointments for a doctor | `200 OK` |
| `GET` | `/api/appointments/status/{status}` | Filter appointments by status (`PENDING`, `CONFIRMED`, etc.) | `200 OK` |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancel an active appointment (`?reason=...`) | `200 OK` |
| `PATCH` | `/api/appointments/{id}/reschedule` | Reschedule an appointment to a new date and time | `200 OK` |
| `PATCH` | `/api/appointments/{id}/status` | Update appointment status (`COMPLETED`, `REJECTED`, `NO_SHOW`) | `200 OK` |

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

### 12. Register Doctor

Registers a new doctor and assigns them to an existing clinical department. Enforces uniqueness on email and medical license number.

- **URL:** `POST /api/doctors`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "fullName": "Dr. John Watson",
  "email": "dr.watson@hospital.com",
  "phoneNumber": "9876543210",
  "gender": "Male",
  "specialization": "Cardiologist",
  "qualification": "MBBS, MD Cardiology",
  "experienceYears": 12,
  "licenseNumber": "MED-LIC-1001",
  "consultationFee": 1500.00,
  "about": "Senior consultant in interventional cardiology and preventive cardiovascular health.",
  "profileImageUrl": "https://images.hospital.com/doctors/watson.jpg",
  "departmentId": 1
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "fullName": "Dr. John Watson",
  "email": "dr.watson@hospital.com",
  "phoneNumber": "9876543210",
  "gender": "Male",
  "specialization": "Cardiologist",
  "qualification": "MBBS, MD Cardiology",
  "experienceYears": 12,
  "licenseNumber": "MED-LIC-1001",
  "consultationFee": 1500.00,
  "about": "Senior consultant in interventional cardiology and preventive cardiovascular health.",
  "profileImageUrl": "https://images.hospital.com/doctors/watson.jpg",
  "departmentId": 1,
  "departmentName": "Cardiology",
  "createdAt": "2026-11-16T14:10:00",
  "updatedAt": "2026-11-16T14:10:00"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/doctors \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dr. John Watson",
    "email": "dr.watson@hospital.com",
    "phoneNumber": "9876543210",
    "gender": "Male",
    "specialization": "Cardiologist",
    "qualification": "MBBS, MD Cardiology",
    "experienceYears": 12,
    "licenseNumber": "MED-LIC-1001",
    "consultationFee": 1500.00,
    "about": "Senior consultant in interventional cardiology.",
    "profileImageUrl": "https://images.hospital.com/doctors/watson.jpg",
    "departmentId": 1
  }'
```

---

### 13. Get All Doctors (Paginated)

Retrieves a paginated list of doctors with configurable page index, page size, and sorting field.

- **URL:** `GET /api/doctors?page=0&size=10&sort=experienceYears,desc`
- **Query Parameters:**
  - `page` (optional, default: `0`)
  - `size` (optional, default: `10`)
  - `sort` (optional, default: `id,asc`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/doctors?page=0&size=10&sort=experienceYears,desc"
```

---

### 14. Get Doctor by ID

Retrieves complete doctor profile with department details.

- **URL:** `GET /api/doctors/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/doctors/1
```

---

### 15. Get Doctors by Department

Retrieves all doctors affiliated with a specific department.

- **URL:** `GET /api/doctors/department/{departmentId}`
- **Path Parameter:** `departmentId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/doctors/department/1
```

---

### 16. Search Doctors (Multi-Criteria)

Performs dynamic search across 5 optional filter criteria with pagination. Ideal for user searches and chatbot tool invocations.

- **URL:** `GET /api/doctors/search`
- **Query Parameters:**
  - `name`: Partial doctor name (e.g. `watson`)
  - `specialization`: Case-insensitive specialization (e.g. `Cardiologist`)
  - `departmentId`: Department ID (e.g. `1`)
  - `minExperience`: Minimum years of clinical experience (e.g. `5`)
  - `maxFee`: Maximum consultation fee (e.g. `2000.00`)
  - `page`, `size`, `sort`: Standard pagination

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/doctors/search?specialization=Cardiologist&maxFee=2000.00&minExperience=5"
```

---

### 17. Update Doctor

Updates doctor profile, qualifications, fees, or affiliated department.

- **URL:** `PUT /api/doctors/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/doctors/1 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Dr. John Watson",
    "email": "dr.watson@hospital.com",
    "phoneNumber": "9876543210",
    "gender": "Male",
    "specialization": "Senior Cardiologist",
    "qualification": "MBBS, MD, FACC",
    "experienceYears": 15,
    "licenseNumber": "MED-LIC-1001",
    "consultationFee": 1800.00,
    "about": "Chief of interventional cardiology.",
    "profileImageUrl": "https://images.hospital.com/doctors/watson.jpg",
    "departmentId": 1
  }'
```

---

### 18. Delete Doctor

Deletes doctor profile by ID.

- **URL:** `DELETE /api/doctors/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X DELETE http://localhost:8080/api/doctors/1
```

---

### 19. Add Working Schedule

Configures a recurring weekly consultation shift for a doctor on a specific day of the week. Enforces start time < end time and checks for shift overlaps.

- **URL:** `POST /api/doctors/{doctorId}/availability`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "13:00:00",
  "slotDurationMinutes": 30,
  "isAvailable": true
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "doctorId": 1,
  "doctorName": "Dr. John Watson",
  "dayOfWeek": "MONDAY",
  "startTime": "09:00:00",
  "endTime": "13:00:00",
  "slotDurationMinutes": 30,
  "isAvailable": true
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/doctors/1/availability \
  -H "Content-Type: application/json" \
  -d '{
    "dayOfWeek": "MONDAY",
    "startTime": "09:00:00",
    "endTime": "13:00:00",
    "slotDurationMinutes": 30,
    "isAvailable": true
  }'
```

---

### 20. Get Doctor Schedule

Retrieves all configured weekly shifts for a doctor.

- **URL:** `GET /api/doctors/{doctorId}/availability`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/doctors/1/availability
```

---

### 21. Get Available Slots for Date

Calculates and returns granular, bookable consultation time slots for any target calendar date.

- **URL:** `GET /api/doctors/{doctorId}/slots?date=YYYY-MM-DD`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)
- **Query Parameter:** `date` (ISO-8601 date, e.g., `2026-11-23`)

#### Success Response (`200 OK`)
```json
[
  {
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "isAvailable": true
  },
  {
    "startTime": "09:30:00",
    "endTime": "10:00:00",
    "isAvailable": true
  }
]
```

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/doctors/1/slots?date=2026-11-23"
```

---

### 22. Update Working Schedule

Updates an existing consultation shift's hours or active status with overlap checks.

- **URL:** `PUT /api/doctors/{doctorId}/availability/{availabilityId}`
- **Path Parameters:**
  - `doctorId` (Long, e.g., `1`)
  - `availabilityId` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/doctors/1/availability/1 \
  -H "Content-Type: application/json" \
  -d '{
    "dayOfWeek": "MONDAY",
    "startTime": "10:00:00",
    "endTime": "14:00:00",
    "slotDurationMinutes": 30,
    "isAvailable": true
  }'
```

---

### 23. Delete Working Schedule

Deletes a doctor's scheduled shift.

- **URL:** `DELETE /api/doctors/{doctorId}/availability/{availabilityId}`
- **Path Parameters:**
  - `doctorId` (Long, e.g., `1`)
  - `availabilityId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X DELETE http://localhost:8080/api/doctors/1/availability/1
```

---

### 24. Book Appointment

Books a clinical consultation. Validates doctor schedule, ensures the date is in the future, detects double bookings for both doctor and patient, and snapshots the consultation fee.

- **URL:** `POST /api/appointments`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "patientId": 1,
  "doctorId": 1,
  "appointmentDate": "2026-11-23",
  "startTime": "10:00:00",
  "reason": "Chest pain and cardiovascular follow-up",
  "notes": "Patient reports shortness of breath on exertion."
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "patientId": 1,
  "patientName": "Jane Doe",
  "patientEmail": "jane.doe@example.com",
  "patientPhone": "9876543210",
  "doctorId": 1,
  "doctorName": "Dr. John Watson",
  "doctorSpecialization": "Cardiologist",
  "departmentName": "Cardiology",
  "appointmentDate": "2026-11-23",
  "startTime": "10:00:00",
  "endTime": "10:30:00",
  "status": "CONFIRMED",
  "reason": "Chest pain and cardiovascular follow-up",
  "consultationFee": 1500.00,
  "notes": "Patient reports shortness of breath on exertion.",
  "createdAt": "2026-11-16T14:20:00",
  "updatedAt": "2026-11-16T14:20:00"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/appointments \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "doctorId": 1,
    "appointmentDate": "2026-11-23",
    "startTime": "10:00:00",
    "reason": "Chest pain and cardiovascular follow-up"
  }'
```

---

### 25. Get Appointment by ID

Retrieves complete appointment details.

- **URL:** `GET /api/appointments/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/appointments/1
```

---

### 26. Get Appointments by Patient

Retrieves paginated appointment history for a patient.

- **URL:** `GET /api/appointments/patient/{patientId}?page=0&size=10`
- **Path Parameter:** `patientId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/appointments/patient/1"
```

---

### 27. Get Appointments by Doctor

Retrieves paginated consultation schedule for a doctor.

- **URL:** `GET /api/appointments/doctor/{doctorId}?page=0&size=10`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/appointments/doctor/1"
```

---

### 28. Filter Appointments by Status

Retrieves appointments matching a specific lifecycle status (`PENDING`, `CONFIRMED`, `CANCELLED`, `COMPLETED`, `REJECTED`, `NO_SHOW`).

- **URL:** `GET /api/appointments/status/{status}?page=0&size=10`
- **Path Parameter:** `status` (e.g. `CONFIRMED`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/appointments/status/CONFIRMED"
```

---

### 29. Cancel Appointment

Cancels an active (`PENDING` or `CONFIRMED`) appointment.

- **URL:** `PATCH /api/appointments/{id}/cancel`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Query Parameter:** `reason` (optional string)

#### cURL Example
```bash
curl -X PATCH "http://localhost:8080/api/appointments/1/cancel?reason=Emergency+travel"
```

---

### 30. Reschedule Appointment

Moves an appointment to a new date and start time with full availability and overlap re-validation.

- **URL:** `PATCH /api/appointments/{id}/reschedule`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "appointmentDate": "2026-11-24",
  "startTime": "11:00:00",
  "reason": "Requested afternoon slot"
}
```

#### cURL Example
```bash
curl -X PATCH http://localhost:8080/api/appointments/1/reschedule \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentDate": "2026-11-24",
    "startTime": "11:00:00",
    "reason": "Requested afternoon slot"
  }'
```

---

### 31. Update Appointment Status

Allows medical or administrative staff to transition appointment state (`COMPLETED`, `REJECTED`, `NO_SHOW`).

- **URL:** `PATCH /api/appointments/{id}/status`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "status": "COMPLETED",
  "notes": "Consultation concluded successfully."
}
```

#### cURL Example
```bash
curl -X PATCH http://localhost:8080/api/appointments/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "notes": "Consultation concluded successfully."
  }'
```

---

## 💊 Prescription Endpoints (`/api/prescriptions`)

### 32. Create Prescription

Creates a digital medical prescription for a completed appointment. Enforces that the appointment exists, is in `COMPLETED` status, and has not already been prescribed.

- **URL:** `POST /api/prescriptions`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "appointmentId": 1,
  "diagnosis": "Acute Bronchitis and upper respiratory congestion",
  "medicines": "1. Amoxicillin 500mg - 3 times daily for 7 days\n2. Paracetamol 650mg - as needed for fever\n3. Cough expectorant syrup - 10ml twice daily",
  "instructions": "Drink plenty of warm liquids. Rest adequately. Return for follow-up if symptoms persist after 5 days."
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "appointmentId": 1,
  "patientId": 1,
  "patientName": "Jane Doe",
  "doctorId": 1,
  "doctorName": "Dr. John Watson",
  "doctorSpecialization": "Cardiologist",
  "diagnosis": "Acute Bronchitis and upper respiratory congestion",
  "medicines": "1. Amoxicillin 500mg - 3 times daily for 7 days\n2. Paracetamol 650mg - as needed for fever\n3. Cough expectorant syrup - 10ml twice daily",
  "instructions": "Drink plenty of warm liquids. Rest adequately. Return for follow-up if symptoms persist after 5 days.",
  "prescribedAt": "2026-11-16T14:20:00"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/prescriptions \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "diagnosis": "Acute Bronchitis",
    "medicines": "Amoxicillin 500mg TDS x 7d",
    "instructions": "Take after meals. Drink warm fluids."
  }'
```

---

### 33. Get Prescription by ID

Retrieves a single prescription record by its ID.

- **URL:** `GET /api/prescriptions/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/prescriptions/1
```

---

### 34. Get Prescription by Appointment

Retrieves the prescription linked to a specific appointment ID.

- **URL:** `GET /api/prescriptions/appointment/{appointmentId}`
- **Path Parameter:** `appointmentId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/prescriptions/appointment/1
```

---

### 35. Get Prescriptions by Patient

Retrieves paginated prescription history for a patient.

- **URL:** `GET /api/prescriptions/patient/{patientId}?page=0&size=10`
- **Path Parameter:** `patientId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/prescriptions/patient/1?page=0&size=10"
```

---

### 36. Get Prescriptions by Doctor

Retrieves paginated prescriptions authored by a doctor.

- **URL:** `GET /api/prescriptions/doctor/{doctorId}?page=0&size=10`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/prescriptions/doctor/1?page=0&size=10"
```

---

### 37. Update Prescription

Updates diagnosis, medicines, or instructions on an existing prescription.

- **URL:** `PUT /api/prescriptions/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "appointmentId": 1,
  "diagnosis": "Acute Bronchitis - recovering",
  "medicines": "1. Paracetamol 650mg - SOS\n2. Vitamin C 500mg daily",
  "instructions": "Continue hydration and rest."
}
```

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/prescriptions/1 \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "diagnosis": "Acute Bronchitis - recovering",
    "medicines": "Vitamin C 500mg daily",
    "instructions": "Continue hydration."
  }'
```

---

## ⭐ Review & Rating Endpoints (`/api/reviews`)

### 38. Submit Review

Submits a 1-to-5 star rating and feedback comment for a completed appointment. Enforces that the appointment is in `COMPLETED` status and has not already been reviewed.

- **URL:** `POST /api/reviews`
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "appointmentId": 1,
  "rating": 5,
  "comment": "Dr. Watson was extraordinarily attentive and accurate with the diagnosis. Highly recommend!"
}
```

#### Success Response (`201 Created`)
```json
{
  "id": 1,
  "appointmentId": 1,
  "patientId": 1,
  "patientName": "Jane Doe",
  "doctorId": 1,
  "doctorName": "Dr. John Watson",
  "doctorSpecialization": "Cardiologist",
  "rating": 5,
  "comment": "Dr. Watson was extraordinarily attentive and accurate with the diagnosis. Highly recommend!",
  "createdAt": "2026-11-16T14:30:00"
}
```

#### cURL Example
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "appointmentId": 1,
    "rating": 5,
    "comment": "Dr. Watson was extraordinarily attentive and accurate with the diagnosis. Highly recommend!"
  }'
```

---

### 39. Get Review by ID

Retrieves a review record by its primary key ID.

- **URL:** `GET /api/reviews/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/reviews/1
```

---

### 40. Get Review by Appointment

Retrieves the review associated with a specific appointment ID.

- **URL:** `GET /api/reviews/appointment/{appointmentId}`
- **Path Parameter:** `appointmentId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/reviews/appointment/1
```

---

### 41. Get Reviews by Doctor

Retrieves paginated reviews for a doctor, sorted by most recent first.

- **URL:** `GET /api/reviews/doctor/{doctorId}?page=0&size=10`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/reviews/doctor/1?page=0&size=10"
```

---

### 42. Get Doctor Rating Summary

Retrieves real-time aggregated rating statistics for a doctor including the average score (rounded to 2 decimals) and total review count.

- **URL:** `GET /api/reviews/doctor/{doctorId}/summary`
- **Path Parameter:** `doctorId` (Long, e.g., `1`)

#### Success Response (`200 OK`)
```json
{
  "doctorId": 1,
  "doctorName": "Dr. John Watson",
  "doctorSpecialization": "Cardiologist",
  "averageRating": 4.85,
  "totalReviews": 12
}
```

#### cURL Example
```bash
curl -X GET http://localhost:8080/api/reviews/doctor/1/summary
```

---

### 43. Get Reviews by Patient

Retrieves paginated reviews submitted by a specific patient.

- **URL:** `GET /api/reviews/patient/{patientId}?page=0&size=10`
- **Path Parameter:** `patientId` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X GET "http://localhost:8080/api/reviews/patient/1?page=0&size=10"
```

---

### 44. Update Review

Allows a patient to update the rating and comment of their existing review.

- **URL:** `PUT /api/reviews/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)
- **Headers:** `Content-Type: application/json`

#### Request Body
```json
{
  "rating": 4,
  "comment": "Follow-up was slightly delayed, but excellent medical advice overall."
}
```

#### cURL Example
```bash
curl -X PUT http://localhost:8080/api/reviews/1 \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 4,
    "comment": "Follow-up was slightly delayed, but excellent medical advice overall."
  }'
```

---

### 45. Delete Review

Deletes an existing review.

- **URL:** `DELETE /api/reviews/{id}`
- **Path Parameter:** `id` (Long, e.g., `1`)

#### cURL Example
```bash
curl -X DELETE http://localhost:8080/api/reviews/1
```

---

## 🔒 Validation Rules

The application uses Jakarta Validation annotations across all request models:

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

### Doctor Validation (`DoctorRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `fullName` | `@NotBlank`, `@Size(min = 2, max = 150)` | `"Doctor full name is required"` |
| `email` | `@NotBlank`, `@Email` | `"Email is required"`, `"Invalid email format"` |
| `phoneNumber` | `@NotBlank`, `@Pattern(regexp = "^[0-9]{10,15}$")` | `"Phone number is required"`, `"Phone number must contain 10 to 15 digits"` |
| `gender` | `@NotBlank` | `"Gender is required"` |
| `specialization` | `@NotBlank` | `"Specialization is required"` |
| `qualification` | `@NotBlank` | `"Qualification is required"` |
| `experienceYears` | `@NotNull`, `@PositiveOrZero` | `"Experience in years is required"`, `"Experience years cannot be negative"` |
| `licenseNumber` | `@NotBlank` | `"Medical license number is required"` |
| `consultationFee` | `@NotNull`, `@DecimalMin("0.00")` | `"Consultation fee is required"`, `"Consultation fee cannot be negative"` |
| `about` | `@Size(max = 2000)` | `"About text cannot exceed 2000 characters"` |
| `profileImageUrl` | `@Size(max = 500)` | `"Profile image URL cannot exceed 500 characters"` |
| `departmentId` | `@NotNull` | `"Department ID is required"` |

### Doctor Availability Validation (`DoctorAvailabilityRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `dayOfWeek` | `@NotNull` | `"Day of week is required"` |
| `startTime` | `@NotNull` | `"Start time is required"` |
| `endTime` | `@NotNull` | `"End time is required"` (Must be after `startTime`) |
| `slotDurationMinutes` | `@NotNull`, `@Min(10)`, `@Max(120)` | `"Slot duration in minutes is required"`, `"Slot duration must be between 10 and 120 minutes"` |
| `isAvailable` | Optional (Boolean) | Defaults to `true` |

### Appointment Booking Validation (`AppointmentBookingRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `patientId` | `@NotNull` | `"Patient ID is required"` |
| `doctorId` | `@NotNull` | `"Doctor ID is required"` |
| `appointmentDate` | `@NotNull`, `@FutureOrPresent` | `"Appointment date cannot be in the past"` |
| `startTime` | `@NotNull` | `"Start time is required"` |
| `reason` | `@NotBlank`, `@Size(max = 500)` | `"Reason for consultation is required"` |
| `notes` | `@Size(max = 2000)` | `"Notes cannot exceed 2000 characters"` |

### Appointment Reschedule Validation (`AppointmentRescheduleRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `appointmentDate` | `@NotNull`, `@FutureOrPresent` | `"New appointment date cannot be in the past"` |
| `startTime` | `@NotNull` | `"New start time is required"` |
| `reason` | `@Size(max = 500)` | Optional explanation |

### Prescription Validation (`PrescriptionRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `appointmentId` | `@NotNull` | `"Appointment ID is required"` |
| `diagnosis` | `@NotBlank`, `@Size(max = 1000)` | `"Diagnosis is required"`, `"Diagnosis cannot exceed 1000 characters"` |
| `medicines` | `@NotBlank`, `@Size(max = 2000)` | `"Medicines and dosage are required"`, `"Medicines cannot exceed 2000 characters"` |
| `instructions` | `@Size(max = 2000)` | `"Instructions cannot exceed 2000 characters"` |

### Review Submission Validation (`ReviewRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `appointmentId` | `@NotNull` | `"Appointment ID is required"` |
| `rating` | `@NotNull`, `@Min(1)`, `@Max(5)` | `"Rating is required"`, `"Rating must be at least 1"`, `"Rating cannot exceed 5"` |
| `comment` | `@Size(max = 2000)` | `"Comment cannot exceed 2000 characters"` |

### Review Update Validation (`ReviewUpdateRequest`)

| Field | Constraints | Error Message |
| :--- | :--- | :--- |
| `rating` | `@NotNull`, `@Min(1)`, `@Max(5)` | `"Rating is required"`, `"Rating must be at least 1"`, `"Rating cannot exceed 5"` |
| `comment` | `@Size(max = 2000)` | `"Comment cannot exceed 2000 characters"` |

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
- [x] **Phase 3 — Doctor Management:** Doctor profiles, department relationships, pagination, multi-criteria filtering, unit & MockMvc test suites.
- [x] **Phase 4 — Doctor Availability:** Weekly consultation schedules, slots generation, unavailable day flags, unit & MockMvc test suites.
- [x] **Phase 5 — Appointment Scheduling Engine:** Concurrency-safe slot booking, rescheduling, cancellations, status transitions, unit & MockMvc test suites.
- [x] **Phase 6 — Prescription Management:** Post-consultation diagnoses, medicines, instructions, completed status enforcement, unit & MockMvc test suites.
- [x] **Phase 7 — Review & Rating:** Verified reviews for completed appointments, rating aggregation, unit & MockMvc test suites.
- [ ] **Phase 8 — Authentication & Authorization:** Spring Security, BCrypt, JWT, Role-Based Access Control (RBAC).
- [ ] **Phase 9 — AI Healthcare Assistant (Google Gemini):** Spring AI ChatClient, tools calling, session isolation.
- [ ] **Phase 10 — Notifications & Enhancements:** Automated email/SMS reminders, Dockerization.

---

## 👨‍💻 Author

- **Prince Raj** ([@Geet-Prince](https://github.com/Geet-Prince))
- Website: [https://Geetprince.me](https://Geetprince.me)
