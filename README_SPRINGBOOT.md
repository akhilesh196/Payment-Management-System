# Payment Management System API - Usage Guide

## Overview
This is a Spring Boot REST API for managing payments with role-based access control and JWT authentication.

## Base URL
```
http://localhost:8080/api
```

## Authentication
The API uses JWT (JSON Web Token) for authentication. Include the token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

## User Roles and Permissions

### ADMIN
- Full access to all endpoints
- Can create, read, update, delete users and payments
- Can manage all system operations

### FINANCE_MANAGER
- Can view all payments
- Can create and update payments
- Can view all users
- Cannot delete payments or manage users

### VIEWER
- Can view only their own payments
- Can create payments
- Cannot update or delete payments
- Cannot view other users

## Default Users
The system comes with default users for testing:

1. **Admin User**
    - Email: `admin@paymentms.com`
    - Password: `admin123`
    - Role: ADMIN

2. **Finance Manager**
    - Email: `finance@paymentms.com`
    - Password: `finance123`
    - Role: FINANCE_MANAGER

3. **Viewer**
    - Email: `viewer@paymentms.com`
    - Password: `viewer123`
    - Role: VIEWER

## API Endpoints

### Authentication Endpoints

#### 1. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "admin@paymentms.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "System Administrator",
  "email": "admin@paymentms.com",
  "role": "ADMIN"
}
```

#### 2. Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "role": "VIEWER"
}
```

### User Management Endpoints

#### 3. Create User (Admin Only)
```http
POST /api/users
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "name": "Jane Smith",
  "email": "jane@example.com",
  "password": "password123",
  "role": "FINANCE_MANAGER"
}
```

#### 4. Get All Users (Admin/Finance Manager)
```http
GET /api/users
Authorization: Bearer <token>
```

#### 5. Get User by ID (Admin/Finance Manager)
```http
GET /api/users/1
Authorization: Bearer <token>
```

### Payment Management Endpoints

#### 6. Create Payment
```http
POST /api/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 1500.00,
  "paymentType": "OUTGOING",
  "category": "SALARY",
  "date": "2025-08-13T10:30:00",
  "description": "Monthly salary payment"
}
```

#### 7. Get All Payments
```http
GET /api/payments
Authorization: Bearer <token>
```

#### 8. Get Payment by ID
```http
GET /api/payments/1
Authorization: Bearer <token>
```

#### 9. Update Payment (Admin/Finance Manager)
```http
PUT /api/payments/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": 1600.00,
  "status": "COMPLETED",
  "description": "Updated salary payment"
}
```

#### 10. Delete Payment (Admin Only)
```http
DELETE /api/payments/1
Authorization: Bearer <admin-token>
```

#### 11. Get Payments by Status
```http
GET /api/payments/status/PENDING
Authorization: Bearer <token>
```

### Health Check Endpoints

#### 12. Public Health Check
```http
GET /api/test/public
```

#### 13. System Health
```http
GET /api/test/health
```

## Data Models

### Payment Types
- `INCOMING`: Money coming into the system
- `OUTGOING`: Money going out of the system

### Payment Categories
- `SALARY`: Salary payments
- `VENDOR`: Vendor payments
- `INVOICE`: Invoice payments

### Payment Status
- `PENDING`: Initial status
- `PROCESSING`: Being processed
- `COMPLETED`: Successfully completed
- `REJECTED`: Rejected payment

## Error Responses

The API returns standardized error responses:

```json
{
  "status": 404,
  "message": "Payment not found with id: 1",
  "timestamp": "2025-08-13T04:15:22"
}
```

For validation errors:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-08-13T04:15:22",
  "validationErrors": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  }
}
```

## Getting Started

1. **Clone the repository**
2. **Setup PostgreSQL database**
3. **Update application.properties with your database credentials**
4. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```
5. **Test the API using the default users or create new ones**

## Testing with curl

### Login and get token:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@paymentms.com","password":"admin123"}'
```

### Create a payment:
```bash
curl -X POST http://localhost:8080/api/payments \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "paymentType": "OUTGOING",
    "category": "VENDOR",
    "date": "2025-08-13T10:00:00",
    "description": "Vendor payment"
  }'
```

### Get all payments:
```bash
curl -X GET http://localhost:8080/api/payments \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```