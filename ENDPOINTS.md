# Curriculum Tracking System — API Reference

**Base URL:** `http://localhost:8080/api/v1`

All endpoints return responses wrapped in the standard envelope below unless stated otherwise:

```json
{
  "message": "Human-readable status message",
  "data": { ... }
}
```

### Authentication
All protected endpoints require a JWT Bearer token in the `Authorization` header:
```
Authorization: Bearer <access_token>
```

### Pagination Query Parameters
Paginated endpoints accept the following query parameters:
| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Zero-based page index |
| `size` | `20` | Items per page |
| `sort` | varies | Field to sort by |

---

## Table of Contents
1. [Authentication](#1-authentication)
2. [Password Reset](#2-password-reset)
3. [Users](#3-users)
4. [Schools](#4-schools)
5. [Departments — Public](#5-departments--public)
6. [Departments — Admin](#6-departments--admin)
7. [Curriculums — Public](#7-curriculums--public)
8. [Curriculums — Admin](#8-curriculums--admin)
9. [Statistics](#9-statistics)
10. [Curriculum Tracking](#10-curriculum-tracking)
11. [Tracking Documents](#11-tracking-documents)
12. [Tracking Steps](#12-tracking-steps)
13. [Enums Reference](#13-enums-reference)

---

## 1. Authentication

Base path: `/api/v1/auth`

---

### POST `/api/v1/auth/login`
Authenticate a user and receive access/refresh tokens.

**Auth required:** No

**Request body:**
```json
{
  "username": "john.doe",
  "password": "SecurePass123"
}
```

**Success response `200 OK`:**
```json
{
  "message": "Successfully logged in",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@university.ac.ke",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_ADMIN"]
  }
}
```

---

### POST `/api/v1/auth/refresh`
Exchange a refresh token for a new access token.

**Auth required:** No

**Request body:**
```json
{
  "refreshToken": "dGhpcyBpcyBhIHJlZnJlc2ggdG9rZW4..."
}
```

**Success response `200 OK`:**
```json
{
  "message": "Successfully refreshed token",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "bmV3UmVmcmVzaFRva2Vu...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@university.ac.ke",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_ADMIN"]
  }
}
```

---

### GET `/api/v1/auth/profile`
Get the full profile of the currently authenticated user.

**Auth required:** Yes

**Success response `200 OK`:**
```json
{
  "message": "Successfully retrieved profile",
  "data": {
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@university.ac.ke",
    "firstName": "John",
    "lastName": "Doe",
    "roles": ["ROLE_ADMIN"],
    "permissions": {
      "isAdmin": true,
      "isDean": false,
      "isViceChancellor": false,
      "canManageUsers": true
    }
  }
}
```

---

### GET `/api/v1/auth/validate`
Check whether the current token is valid and retrieve basic claims.

**Auth required:** Yes

**Success response `200 OK` (valid token):**
```json
{
  "message": "Success",
  "data": {
    "valid": true,
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@university.ac.ke",
    "roles": ["ROLE_ADMIN"],
    "permissions": {
      "isAdmin": true,
      "isDean": false,
      "isViceChancellor": false,
      "canManageUsers": true
    }
  }
}
```

**Response when invalid:**
```json
{
  "message": "Success",
  "data": {
    "valid": false,
    "message": "Invalid or expired token"
  }
}
```

---

### GET `/api/v1/auth/roles`
Get roles and permissions for the authenticated user.

**Auth required:** Yes

**Success response `200 OK`:**
```json
{
  "message": "Success",
  "data": {
    "roles": ["ROLE_QA"],
    "permissions": {
      "isAdmin": false,
      "isDean": false,
      "isViceChancellor": false,
      "canManageUsers": false
    }
  }
}
```

---

### GET `/api/v1/auth/user-info`
Get basic identity fields for the authenticated user.

**Auth required:** Yes

**Success response `200 OK`:**
```json
{
  "message": "Success",
  "data": {
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@university.ac.ke",
    "firstName": "John",
    "lastName": "Doe"
  }
}
```

---

### GET `/api/v1/auth/permissions`
Get only the permissions map for the authenticated user.

**Auth required:** Yes

**Success response `200 OK`:**
```json
{
  "message": "Success",
  "data": {
    "permissions": {
      "isAdmin": false,
      "isDean": true,
      "isViceChancellor": false,
      "canManageUsers": false
    }
  }
}
```

---

### POST `/api/v1/auth/check-role`
Check whether the authenticated user has a specific role.

**Auth required:** Yes

**Query parameter:** `roleName=ADMIN`

**Example:**
```
POST /api/v1/auth/check-role?roleName=ADMIN
```

**Success response `200 OK`:**
```json
{
  "message": "Success",
  "data": {
    "hasRole": true
  }
}
```

---

### POST `/api/v1/auth/check-any-role`
Check whether the authenticated user has at least one of the specified roles.

**Auth required:** Yes

**Query parameter:** `roleNames=ADMIN&roleNames=DEAN`

**Example:**
```
POST /api/v1/auth/check-any-role?roleNames=ADMIN&roleNames=DEAN
```

**Success response `200 OK`:**
```json
{
  "message": "Success",
  "data": {
    "hasAnyRole": true
  }
}
```

---

## 2. Password Reset

Base path: `/api/v1/auth/password`

---

### POST `/api/v1/auth/password/forgot`
Trigger a password reset email for the given address.

**Auth required:** No

**Request body:**
```json
{
  "email": "john.doe@university.ac.ke"
}
```

**Success response `200 OK`:**
```json
{
  "message": "If the email exists in our system, a password reset link has been sent to your email address",
  "data": null
}
```

---

### POST `/api/v1/auth/password/validate-token`
Validate whether a password reset token is still valid.

**Auth required:** No

**Query parameter:** `token=<reset_token>`

**Example:**
```
POST /api/v1/auth/password/validate-token?token=abc123xyz
```

**Success response `200 OK`:**
```json
{
  "message": "Reset token is valid",
  "data": true
}
```

**Response when invalid `400 Bad Request`:**
```json
{
  "message": "Invalid or expired reset token",
  "data": false
}
```

---

### POST `/api/v1/auth/password/reset`
Set a new password using the reset token received via email.

**Auth required:** No

**Request body:**
```json
{
  "token": "abc123xyz",
  "newPassword": "NewSecurePass456"
}
```

> `newPassword` must be between 8 and 128 characters.

**Success response `200 OK`:**
```json
{
  "message": "Password has been reset successfully. You can now login with your new password",
  "data": null
}
```

---

## 3. Users

Base path: `/api/v1/users`

---

### POST `/api/v1/users/create`
Create a new system user.

**Auth required:** Yes — `ADMIN` only

**Request body:**
```json
{
  "username": "jane.smith",
  "email": "jane.smith@university.ac.ke",
  "password": "SecurePass123",
  "firstName": "Jane",
  "lastName": "Smith",
  "phoneNumber": "+254712345678",
  "roleName": "DEAN"
}
```

> `roleName` is optional. Supported values: `ADMIN`, `DEAN`, `QA`, `HOD`, `VICE_CHANCELLOR`.

**Success response `200 OK`:**
```json
{
  "message": "Successfully created user",
  "data": {
    "id": 5,
    "username": "jane.smith",
    "email": "jane.smith@university.ac.ke",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+254712345678",
    "enabled": true,
    "roles": ["ROLE_DEAN"],
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:00:00"
  }
}
```

---

### POST `/api/v1/users/assign-role`
Assign an additional role to an existing user.

**Auth required:** Yes — `ADMIN` only

**Request body:**
```json
{
  "userId": 5,
  "roleName": "QA"
}
```

**Success response `200 OK`:**
```json
{
  "message": "Successfully assigned role to user",
  "data": {
    "id": 5,
    "username": "jane.smith",
    "email": "jane.smith@university.ac.ke",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+254712345678",
    "enabled": true,
    "roles": ["ROLE_DEAN", "ROLE_QA"],
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:15:00"
  }
}
```

---

### DELETE `/api/v1/users/{userId}/roles/{roleName}/delete`
Remove a specific role from a user.

**Auth required:** Yes — `ADMIN` only

**Path parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `userId` | Long | ID of the user |
| `roleName` | String | Name of the role to remove (e.g. `QA`) |

**Example:**
```
DELETE /api/v1/users/5/roles/QA/delete
```

**Success response `200 OK`:**
```json
{
  "message": "Successfully removed user role",
  "data": {
    "id": 5,
    "username": "jane.smith",
    "email": "jane.smith@university.ac.ke",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+254712345678",
    "enabled": true,
    "roles": ["ROLE_DEAN"],
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:30:00"
  }
}
```

---

### GET `/api/v1/users/get-all-users`
Retrieve a list of all users in the system.

**Auth required:** Yes — `ADMIN` or `VICE_CHANCELLOR`

**Success response `200 OK`:**
```json
{
  "message": "Successfully retrieved users",
  "data": [
    {
      "id": 1,
      "username": "john.doe",
      "email": "john.doe@university.ac.ke",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+254700000000",
      "enabled": true,
      "roles": ["ROLE_ADMIN"],
      "createdAt": "2025-01-15T08:00:00",
      "updatedAt": "2025-01-15T08:00:00"
    },
    {
      "id": 5,
      "username": "jane.smith",
      "email": "jane.smith@university.ac.ke",
      "firstName": "Jane",
      "lastName": "Smith",
      "phoneNumber": "+254712345678",
      "enabled": true,
      "roles": ["ROLE_DEAN"],
      "createdAt": "2025-05-31T10:00:00",
      "updatedAt": "2025-05-31T10:00:00"
    }
  ]
}
```

---

### GET `/api/v1/users/user/{userId}`
Get a single user by their ID.

**Auth required:** Yes — `ADMIN`, `VICE_CHANCELLOR`, or the user themselves

**Path parameter:** `userId` (Long)

**Example:**
```
GET /api/v1/users/user/5
```

**Success response `200 OK`:**
```json
{
  "message": "Successfully retrieved user",
  "data": {
    "id": 5,
    "username": "jane.smith",
    "email": "jane.smith@university.ac.ke",
    "firstName": "Jane",
    "lastName": "Smith",
    "phoneNumber": "+254712345678",
    "enabled": true,
    "roles": ["ROLE_DEAN"],
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:00:00"
  }
}
```

---

## 4. Schools

Base path: `/api/v1/schools`

---

### GET `/api/v1/schools/get-all`
Retrieve all schools in the system.

**Auth required:** No

**Success response `200 OK`:**
```json
[
  {
    "id": 1,
    "name": "School of Computing and Informatics",
    "code": "SCI",
    "deanId": 3
  },
  {
    "id": 2,
    "name": "School of Engineering",
    "code": "SE",
    "deanId": 7
  }
]
```

> Note: This endpoint returns the array directly — it is not wrapped in the standard `ApiResponse` envelope.

---

### GET `/api/v1/schools/get-by-id/{id}`
Get a single school by its ID.

**Auth required:** No

**Path parameter:** `id` (Long)

**Example:**
```
GET /api/v1/schools/get-by-id/1
```

**Success response `200 OK`:**
```json
{
  "id": 1,
  "name": "School of Computing and Informatics",
  "code": "SCI",
  "deanId": 3
}
```

> Note: Returns the object directly — not wrapped in the standard envelope.

---

## 5. Departments — Public

Base path: `/api/v1/user/departments`

---

### GET `/api/v1/user/departments/get-all-departments`
List all departments with optional text search and pagination.

**Auth required:** No

**Query parameters:**
| Parameter | Default | Description |
|-----------|---------|-------------|
| `page` | `0` | Page index (zero-based) |
| `size` | `10` | Items per page |
| `sortBy` | `name` | Field to sort by |
| `sortDir` | `asc` | Sort direction: `asc` or `desc` |
| `search` | — | Optional search term (name/code) |

**Example:**
```
GET /api/v1/user/departments/get-all-departments?page=0&size=10&search=computer
```

**Success response `200 OK`:**
```json
{
  "message": "Departments retrieved successfully",
  "data": {
    "departments": [
      {
        "id": 1,
        "name": "Department of Computer Science",
        "code": "CS",
        "headId": 4,
        "schoolId": 1,
        "schoolName": "School of Computing and Informatics",
        "curriculumCount": 3,
        "createdAt": "2025-01-10T09:00:00",
        "updatedAt": "2025-03-20T14:00:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 1,
    "pageSize": 10,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/user/departments/school/{schoolId}`
Get all departments belonging to a specific school.

**Auth required:** No

**Path parameter:** `schoolId` (Long)

**Query parameters:** Same pagination + search params as above.

**Example:**
```
GET /api/v1/user/departments/school/1?page=0&size=10
```

**Success response `200 OK`:**
```json
{
  "message": "School departments retrieved successfully",
  "data": {
    "departments": [
      {
        "id": 1,
        "name": "Department of Computer Science",
        "code": "CS",
        "headId": 4,
        "schoolId": 1,
        "schoolName": "School of Computing and Informatics",
        "curriculumCount": 3,
        "createdAt": "2025-01-10T09:00:00",
        "updatedAt": "2025-03-20T14:00:00"
      },
      {
        "id": 2,
        "name": "Department of Information Technology",
        "code": "IT",
        "headId": 6,
        "schoolId": 1,
        "schoolName": "School of Computing and Informatics",
        "curriculumCount": 2,
        "createdAt": "2025-01-10T09:00:00",
        "updatedAt": "2025-02-01T11:00:00"
      }
    ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 2,
    "pageSize": 10,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/user/departments/department/{departmentId}`
Get a single department by ID.

**Auth required:** No

**Path parameter:** `departmentId` (Long)

**Example:**
```
GET /api/v1/user/departments/department/1
```

**Success response `200 OK`:**
```json
{
  "message": "Department retrieved successfully",
  "data": {
    "id": 1,
    "name": "Department of Computer Science",
    "code": "CS",
    "headId": 4,
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "curriculumCount": 3,
    "createdAt": "2025-01-10T09:00:00",
    "updatedAt": "2025-03-20T14:00:00"
  }
}
```

---

### GET `/api/v1/user/departments/school/{schoolId}/count`
Get the total number of departments in a given school.

**Auth required:** No

**Path parameter:** `schoolId` (Long)

**Example:**
```
GET /api/v1/user/departments/school/1/count
```

**Success response `200 OK`:**
```json
{
  "message": "Department count retrieved successfully",
  "data": 4
}
```

---

### GET `/api/v1/user/departments/{departmentId}/exists`
Check whether a department with the given ID exists.

**Auth required:** No

**Path parameter:** `departmentId` (Long)

**Example:**
```
GET /api/v1/user/departments/1/exists
```

**Success response `200 OK`:**
```json
{
  "message": "Department existence check completed",
  "data": true
}
```

---

## 6. Departments — Admin

Base path: `/api/v1/admin/departments`

All endpoints require `ADMIN` role.

---

### POST `/api/v1/admin/departments/create-department`
Create a new department.

**Auth required:** Yes — `ADMIN` only

**Request body:**
```json
{
  "name": "Department of Software Engineering",
  "code": "SE",
  "schoolId": 1,
  "headId": 9
}
```

> `code` (max 10 chars) and `headId` are optional.

**Success response `200 OK`:**
```json
{
  "message": "Department created successfully",
  "data": {
    "id": 5,
    "name": "Department of Software Engineering",
    "code": "SE",
    "headId": 9,
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "curriculumCount": 0,
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:00:00"
  }
}
```

---

### PUT `/api/v1/admin/departments/update/{departmentId}`
Update an existing department.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `departmentId` (Long)

**Request body:** (all fields optional)
```json
{
  "name": "Department of Software Engineering & AI",
  "code": "SEAI",
  "headId": 11
}
```

**Success response `200 OK`:**
```json
{
  "message": "Department updated successfully",
  "data": {
    "id": 5,
    "name": "Department of Software Engineering & AI",
    "code": "SEAI",
    "headId": 11,
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "curriculumCount": 0,
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T11:30:00"
  }
}
```

---

### DELETE `/api/v1/admin/departments/delete/{departmentId}`
Delete a department.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `departmentId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Department deleted successfully",
  "data": null
}
```

---

## 7. Curriculums — Public

Base path: `/api/v1/users/curriculums`

---

### GET `/api/v1/users/curriculums/get-by-id/{curriculumId}`
Get a single curriculum by its ID.

**Auth required:** No

**Path parameter:** `curriculumId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Curriculum retrieved successfully",
  "data": {
    "id": 10,
    "name": "Bachelor of Science in Computer Science",
    "code": "BSC-CS",
    "durationSemesters": 8,
    "status": "APPROVED",
    "createdBy": 1,
    "approvedBy": 2,
    "approvedAt": "2025-03-01T12:00:00",
    "effectiveDate": "2025-09-01T00:00:00",
    "expiryDate": "2030-08-31T00:00:00",
    "isActive": true,
    "createdAt": "2025-01-20T09:00:00",
    "updatedAt": "2025-03-01T12:00:00",
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "departmentId": 1,
    "departmentName": "Department of Computer Science",
    "academicLevelName": "Undergraduate"
  }
}
```

---

### GET `/api/v1/users/curriculums/get-all`
List all curriculums with pagination.

**Auth required:** No

**Query parameters:** Standard pagination params (`page`, `size`, `sort`).

**Success response `200 OK`:**
```json
{
  "message": "Curriculums retrieved successfully",
  "data": {
    "curriculums": [
      {
        "id": 10,
        "name": "Bachelor of Science in Computer Science",
        "code": "BSC-CS",
        "durationSemesters": 8,
        "status": "APPROVED",
        "createdBy": 1,
        "approvedBy": 2,
        "approvedAt": "2025-03-01T12:00:00",
        "effectiveDate": "2025-09-01T00:00:00",
        "expiryDate": "2030-08-31T00:00:00",
        "isActive": true,
        "createdAt": "2025-01-20T09:00:00",
        "updatedAt": "2025-03-01T12:00:00",
        "schoolId": 1,
        "schoolName": "School of Computing and Informatics",
        "departmentId": 1,
        "departmentName": "Department of Computer Science",
        "academicLevelName": "Undergraduate"
      }
    ],
    "currentPage": 0,
    "totalPages": 3,
    "totalElements": 42,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### POST `/api/v1/users/curriculums/search`
Search curriculums by multiple filter criteria.

**Auth required:** No

**Request body:** (all fields optional)
```json
{
  "name": "Computer Science",
  "code": "BSC-CS",
  "status": "APPROVED",
  "schoolId": 1,
  "departmentId": 1,
  "academicLevelId": 2,
  "createdBy": 1,
  "isActive": true
}
```

> `status` values: `PENDING`, `APPROVED`, `REJECTED`, `UNDER_REVIEW`

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Curriculums search completed successfully",
  "data": {
    "curriculums": [ ... ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 2,
    "pageSize": 20,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/users/curriculums/school/{schoolId}`
Get all curriculums belonging to a specific school.

**Auth required:** No

**Path parameter:** `schoolId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "School curriculums retrieved successfully",
  "data": {
    "curriculums": [ ... ],
    "currentPage": 0,
    "totalPages": 2,
    "totalElements": 25,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/users/curriculums/department/{departmentId}`
Get all curriculums belonging to a specific department.

**Auth required:** No

**Path parameter:** `departmentId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Department curriculums retrieved successfully",
  "data": {
    "curriculums": [ ... ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 3,
    "pageSize": 20,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/users/curriculums/academic-level/{academicLevelId}`
Get all curriculums for a specific academic level.

**Auth required:** No

**Path parameter:** `academicLevelId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Academic level curriculums retrieved successfully",
  "data": {
    "curriculums": [ ... ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 10,
    "pageSize": 20,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

## 8. Curriculums — Admin

Base path: `/api/v1/admin/curriculums`

All endpoints require `ADMIN` role.

---

### POST `/api/v1/admin/curriculums/create`
Create a new curriculum.

**Auth required:** Yes — `ADMIN` only

**Request body:**
```json
{
  "name": "Bachelor of Science in Information Technology",
  "code": "BSC-IT",
  "durationSemesters": 8,
  "effectiveDate": "2025-09-01T00:00:00",
  "expiryDate": "2030-08-31T00:00:00",
  "schoolId": 1,
  "departmentId": 2,
  "academicLevelId": 1
}
```

> `code`, `durationSemesters`, `effectiveDate`, and `expiryDate` are optional.

**Success response `201 Created`:**
```json
{
  "message": "Curriculum created successfully",
  "data": {
    "id": 11,
    "name": "Bachelor of Science in Information Technology",
    "code": "BSC-IT",
    "durationSemesters": 8,
    "status": "PENDING",
    "createdBy": 1,
    "approvedBy": null,
    "approvedAt": null,
    "effectiveDate": "2025-09-01T00:00:00",
    "expiryDate": "2030-08-31T00:00:00",
    "isActive": true,
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:00:00",
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "departmentId": 2,
    "departmentName": "Department of Information Technology",
    "academicLevelName": "Undergraduate"
  }
}
```

---

### PUT `/api/v1/admin/curriculums/update/{curriculumId}`
Update an existing curriculum.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `curriculumId` (Long)

**Request body:** (all fields optional)
```json
{
  "name": "Bachelor of Science in Information Technology (Revised)",
  "code": "BSC-IT-R",
  "durationSemesters": 10,
  "effectiveDate": "2026-01-01T00:00:00",
  "expiryDate": "2031-12-31T00:00:00",
  "departmentId": 2,
  "academicLevelId": 1
}
```

**Success response `200 OK`:**
```json
{
  "message": "Curriculum updated successfully",
  "data": { ... }
}
```

---

### DELETE `/api/v1/admin/curriculums/delete/{curriculumId}`
Soft-delete a curriculum (marks it as inactive).

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `curriculumId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Curriculum deleted successfully",
  "data": null
}
```

---

### DELETE `/api/v1/admin/curriculums/permanent-delete/{curriculumId}`
Permanently and irreversibly delete a curriculum.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `curriculumId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Curriculum permanently deleted successfully",
  "data": null
}
```

---

### PUT `/api/v1/admin/curriculums/review/{curriculumId}`
Put a curriculum under review.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `curriculumId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Curriculum put under review successfully",
  "data": {
    "id": 11,
    "status": "UNDER_REVIEW",
    ...
  }
}
```

---

### PUT `/api/v1/admin/curriculums/toggle-status/{curriculumId}`
Toggle a curriculum between active and inactive.

**Auth required:** Yes — `ADMIN` only

**Path parameter:** `curriculumId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Curriculum status toggled successfully",
  "data": {
    "id": 11,
    "isActive": false,
    ...
  }
}
```

---

### GET `/api/v1/admin/curriculums/stats`
Get a count breakdown of curriculums by status.

**Auth required:** Yes — `ADMIN` only

**Success response `200 OK`:**
```json
{
  "message": "Curriculum statistics retrieved successfully",
  "data": {
    "totalCurriculums": 42,
    "pendingCurriculums": 8,
    "approvedCurriculums": 28,
    "rejectedCurriculums": 3,
    "underReviewCurriculums": 3
  }
}
```

---

### GET `/api/v1/admin/curriculums/expiring-soon`
Get a list of curriculums expiring within the specified number of days.

**Auth required:** Yes — `ADMIN` only

**Query parameter:** `days` (default `30`)

**Example:**
```
GET /api/v1/admin/curriculums/expiring-soon?days=60
```

**Success response `200 OK`:**
```json
{
  "message": "Expiring curriculums retrieved successfully",
  "data": [
    {
      "id": 5,
      "name": "Bachelor of Science in Computer Science",
      "code": "BSC-CS",
      "expiryDate": "2025-07-15T00:00:00",
      ...
    }
  ]
}
```

---

## 9. Statistics

Base path: `/api/v1/stats`

---

### GET `/api/v1/stats/summary`
Get a high-level system-wide summary of schools, departments, and curriculums.

**Auth required:** No

**Success response `200 OK`:**
```json
{
  "message": "System statistics retrieved successfully",
  "data": {
    "totalSchools": 6,
    "totalDepartments": 24,
    "totalCurriculums": 42
  }
}
```

---

## 10. Curriculum Tracking

Base path: `/api/v1/tracking`

---

### POST `/api/v1/tracking/initiate`
Initiate a new curriculum tracking process. Accepts `multipart/form-data`.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `schoolId` | Yes | Long | School ID |
| `departmentId` | Yes | Long | Department ID |
| `academicLevelId` | Yes | Long | Academic level ID |
| `proposedCurriculumName` | Yes | String | Proposed curriculum name (max 200) |
| `curriculumId` | No | Long | Existing curriculum ID if revising |
| `proposedCurriculumCode` | No | String | Proposed curriculum code (max 20) |
| `proposedDurationSemesters` | No | Integer | Duration in semesters |
| `curriculumDescription` | No | String | Description (max 2000) |
| `proposedEffectiveDate` | No | DateTime | ISO-8601 format |
| `proposedExpiryDate` | No | DateTime | ISO-8601 format |
| `initialNotes` | No | String | Initial notes (max 1000) |
| `expectedCompletionDate` | No | DateTime | ISO-8601 format |
| `documents` | No | File[] | Initial supporting documents |

**Success response `201 Created`:**
```json
{
  "message": "Curriculum tracking initiated successfully",
  "data": {
    "id": 100,
    "trackingId": "TRK-2025-001",
    "curriculumId": null,
    "curriculumName": null,
    "curriculumCode": null,
    "displayCurriculumName": "BSc in Data Science",
    "displayCurriculumCode": "BSC-DS",
    "proposedCurriculumName": "BSc in Data Science",
    "proposedCurriculumCode": "BSC-DS",
    "proposedDurationSemesters": 8,
    "curriculumDescription": "A comprehensive program covering data science fundamentals",
    "proposedEffectiveDate": "2026-01-01T00:00:00",
    "proposedExpiryDate": "2031-12-31T00:00:00",
    "schoolId": 1,
    "schoolName": "School of Computing and Informatics",
    "departmentId": 1,
    "departmentName": "Department of Computer Science",
    "academicLevelId": 1,
    "academicLevelName": "Undergraduate",
    "currentStage": "IDEATION",
    "currentStageDisplayName": "Curriculum Ideation",
    "status": "INITIATED",
    "statusDisplayName": "Initiated",
    "initiatedByName": "Jane Smith",
    "initiatedByEmail": "jane.smith@university.ac.ke",
    "currentAssigneeName": "Jane Smith",
    "currentAssigneeEmail": "jane.smith@university.ac.ke",
    "initialNotes": "New curriculum proposal for data science",
    "createdAt": "2025-05-31T10:00:00",
    "updatedAt": "2025-05-31T10:00:00",
    "expectedCompletionDate": "2025-12-31T00:00:00",
    "actualCompletionDate": null,
    "isActive": true,
    "isCompleted": false,
    "isIdeationStage": true,
    "recentSteps": [
      {
        "id": 1,
        "stage": "IDEATION",
        "stageDisplayName": "Curriculum Ideation",
        "action": "INITIATE",
        "actionDisplayName": "Initiate Tracking",
        "performedByName": "Jane Smith",
        "performedByEmail": "jane.smith@university.ac.ke",
        "assignedToName": "Jane Smith",
        "assignedToEmail": "jane.smith@university.ac.ke",
        "fromStage": null,
        "toStage": null,
        "notes": "New curriculum proposal for data science",
        "performedAt": "2025-05-31T10:00:00",
        "dueDate": null,
        "isMilestone": true,
        "isStageTransition": true,
        "isForwardMovement": true,
        "isBackwardMovement": false,
        "documents": []
      }
    ]
  }
}
```

---

### POST `/api/v1/tracking/action`
Perform an action (approve, reject, return, submit, etc.) on a tracking record.

**Auth required:** Yes — `QA` or `DEAN`

**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `trackingId` | Yes | Long | ID of the tracking record |
| `action` | Yes | String | Action to perform (see [Enums](#13-enums-reference)) |
| `notes` | No | String | Notes/comments for this action |
| `returnToStage` | No | String | Stage to return to (when action is `RETURN`) |
| `assignToUserId` | No | Long | User to assign the tracking to |
| `dueDate` | No | DateTime | Due date for the next step |
| `isMilestone` | No | Boolean | Whether this step is a milestone (default `false`) |
| `documents` | No | File[] | Supporting documents for this action |

**Example — Approve:**
```
POST /api/v1/tracking/action
Content-Type: multipart/form-data

trackingId=100
action=APPROVE
notes=All documents reviewed and approved
```

**Example — Return for revision:**
```
POST /api/v1/tracking/action
Content-Type: multipart/form-data

trackingId=100
action=RETURN
returnToStage=IDEATION
notes=Please revise the curriculum description
```

**Success response `200 OK`:**
```json
{
  "message": "tracking action performed successfully",
  "data": {
    "id": 100,
    "trackingId": "TRK-2025-001",
    "currentStage": "REVIEW_APPROVAL",
    "currentStageDisplayName": "Review & Tracking Approval",
    "status": "IN_PROGRESS",
    "statusDisplayName": "In Progress",
    ...
  }
}
```

---

### GET `/api/v1/tracking/{trackingId}`
Get full tracking details by the database ID.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Tracking details retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/by-tracking-id/{trackingId}`
Get full tracking details by the human-readable tracking reference string (e.g. `TRK-2025-001`).

**Auth required:** No

**Path parameter:** `trackingId` (String)

**Success response `200 OK`:**
```json
{
  "message": "Tracking details retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking`
List all trackings with pagination.

**Auth required:** No

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Trackings retrieved successfully",
  "data": {
    "trackings": [
      {
        "id": 100,
        "trackingId": "TRK-2025-001",
        "curriculumId": null,
        "curriculumName": null,
        "displayCurriculumName": "BSc in Data Science",
        "displayCurriculumCode": "BSC-DS",
        "proposedCurriculumName": "BSc in Data Science",
        "proposedCurriculumCode": "BSC-DS",
        "schoolId": 1,
        "schoolName": "School of Computing and Informatics",
        "departmentId": 1,
        "departmentName": "Department of Computer Science",
        "academicLevelId": 1,
        "academicLevelName": "Undergraduate",
        "currentStage": "IDEATION",
        "currentStageDisplayName": "Curriculum Ideation",
        "status": "INITIATED",
        "statusDisplayName": "Initiated",
        "initiatedByName": "Jane Smith",
        "currentAssigneeName": "Jane Smith",
        "createdAt": "2025-05-31T10:00:00",
        "expectedCompletionDate": "2025-12-31T00:00:00",
        "isActive": true,
        "isIdeationStage": true
      }
    ],
    "currentPage": 0,
    "totalPages": 5,
    "totalElements": 87,
    "pageSize": 20,
    "hasNext": true,
    "hasPrevious": false,
    "isFirst": true,
    "isLast": false
  }
}
```

---

### POST `/api/v1/tracking/search`
Search trackings with advanced filter criteria.

**Auth required:** No

**Request body:** (all fields optional)
```json
{
  "status": "IN_PROGRESS",
  "currentStage": "REVIEW_APPROVAL",
  "initiatedByUserId": 5,
  "currentAssigneeId": 3,
  "schoolId": 1,
  "departmentId": 1,
  "academicLevelId": 2,
  "curriculumId": 10,
  "searchTerm": "data science",
  "createdAfter": "2025-01-01T00:00:00",
  "createdBefore": "2025-12-31T23:59:59",
  "expectedCompletionBefore": "2025-12-31T00:00:00",
  "isActive": true,
  "isOverdue": false,
  "isIdeationStage": false,
  "hasLinkedCurriculum": false
}
```

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking search completed successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/status/{status}`
Get trackings filtered by status.

**Auth required:** No

**Path parameter:** `status` — one of: `INITIATED`, `IN_PROGRESS`, `APPROVED`, `REJECTED`, `RETURNED_FOR_REVISION`, `COMPLETED`

**Query parameters:** Standard pagination params.

**Example:**
```
GET /api/v1/tracking/status/IN_PROGRESS?page=0&size=10
```

**Success response `200 OK`:**
```json
{
  "message": "Trackings by status retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/stage/{stage}`
Get trackings filtered by their current stage.

**Auth required:** No

**Path parameter:** `stage` — one of: `IDEATION`, `REVIEW_APPROVAL`, `SCHOOL_BOARD`, `DEAN_COMMITTEE`, `SENATE`, `QA_INTERNAL_AUDIT`, `CUE_EXTERNAL_AUDIT`, `VICE_CHANCELLOR_APPROVAL`, `ACCREDITED`

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Trackings by stage retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/my-assignments`
Get trackings currently assigned to the authenticated user.

**Auth required:** Yes

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Assigned trackings retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/my-trackings`
Get trackings initiated by the authenticated user.

**Auth required:** Yes

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Initiated trackings retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/assignee/{userId}`
Get trackings assigned to a specific user (admin view).

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameter:** `userId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Assigned trackings retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/initiator/{userId}`
Get trackings initiated by a specific user (admin view).

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameter:** `userId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Initiated trackings retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/school/{schoolId}`
Get all trackings for a specific school.

**Auth required:** No

**Path parameter:** `schoolId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "School trackings retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/department/{departmentId}`
Get all trackings for a specific department.

**Auth required:** No

**Path parameter:** `departmentId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Department trackings retrieved successfully",
  "data": { ... }
}
```

---

### PUT `/api/v1/tracking/{trackingId}`
Update an existing tracking record.

**Auth required:** Yes — `QA` or `DEAN`

**Content-Type:** `multipart/form-data`

**Path parameter:** `trackingId` (Long)

**Form fields:** Same as `/initiate` (all optional).

**Success response `200 OK`:**
```json
{
  "message": "Tracking updated successfully",
  "data": { ... }
}
```

---

### POST `/api/v1/tracking/{trackingId}/deactivate`
Deactivate a tracking record.

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Tracking deactivated successfully",
  "data": null
}
```

---

### POST `/api/v1/tracking/{trackingId}/reactivate`
Reactivate a previously deactivated tracking record.

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Tracking reactivated successfully",
  "data": { ... }
}
```

---

### POST `/api/v1/tracking/{trackingId}/assign/{assigneeId}`
Reassign a tracking to a different user.

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `trackingId` | Long | Tracking record ID |
| `assigneeId` | Long | User ID to assign to |

**Success response `200 OK`:**
```json
{
  "message": "Tracking assigned successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/{trackingId}/has-permission`
Check whether the authenticated user has permission to act on this tracking.

**Auth required:** Yes

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Permission check completed",
  "data": true
}
```

---

### POST `/api/v1/tracking/{trackingId}/validate-transition/{targetStage}`
Validate whether transitioning to a given stage is allowed for the current state.

**Auth required:** Yes

**Path parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `trackingId` | Long | Tracking record ID |
| `targetStage` | String | Stage enum value to validate against |

**Example:**
```
POST /api/v1/tracking/100/validate-transition/SCHOOL_BOARD
```

**Success response `200 OK`:**
```json
{
  "message": "Stage transition validation completed",
  "data": true
}
```

---

## 11. Tracking Documents

Base path: `/api/v1/tracking/documents`

---

### POST `/api/v1/tracking/documents/upload`
Upload a single document to a specific tracking step.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `file` | Yes | File | The document to upload |
| `trackingId` | Yes | Long | Tracking record ID |
| `stepId` | Yes | Long | Step ID within the tracking |
| `documentType` | No | String | Default `OTHER` (see [Enums](#13-enums-reference)) |
| `description` | No | String | Document description |

**Success response `201 Created`:**
```json
{
  "message": "Document uploaded successfully",
  "data": {
    "id": 50,
    "documentName": "curriculum_proposal_v1_trk100_step1.pdf",
    "originalFilename": "curriculum_proposal.pdf",
    "documentType": "CURRICULUM_PROPOSAL",
    "documentTypeDisplayName": "Curriculum Proposal",
    "filePath": "tracking/100/step/1/curriculum_proposal_v1_trk100_step1.pdf",
    "fileSize": 204800,
    "formattedFileSize": "200 KB",
    "contentType": "application/pdf",
    "fileExtension": "pdf",
    "description": "Initial curriculum proposal document",
    "uploadedByName": "Jane Smith",
    "versionNumber": 1,
    "uploadedAt": "2025-05-31T10:05:00",
    "isActive": true
  }
}
```

---

### POST `/api/v1/tracking/documents/upload/batch`
Upload multiple documents at once to a tracking step.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Content-Type:** `multipart/form-data`

**Form fields:**
| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `files` | Yes | File[] | Array of files |
| `trackingId` | Yes | Long | Tracking record ID |
| `stepId` | Yes | Long | Step ID |
| `documentType` | No | String | Applies to all files (default `OTHER`) |
| `descriptions` | No | String[] | Per-file descriptions (must match file count if provided) |

**Success response `201 Created`:**
```json
{
  "message": "Documents uploaded successfully",
  "data": [
    { ... },
    { ... }
  ]
}
```

---

### GET `/api/v1/tracking/documents/download/{documentId}`
Download the actual file content for a document.

**Auth required:** No

**Path parameter:** `documentId` (Long)

**Response:** Binary file stream with appropriate `Content-Disposition`, `Content-Type`, and `Content-Length` headers.

---

### GET `/api/v1/tracking/documents/download-url/{documentId}`
Get a pre-signed URL to download a document.

**Auth required:** No

**Path parameter:** `documentId` (Long)

**Query parameter:** `expirationMinutes` (default `60`)

**Success response `200 OK`:**
```json
{
  "message": "Download URL generated successfully",
  "data": {
    "downloadUrl": "https://storage.example.com/...",
    "expiresInMinutes": 60
  }
}
```

---

### GET `/api/v1/tracking/documents/{documentId}`
Get metadata for a specific document.

**Auth required:** No

**Path parameter:** `documentId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Document metadata retrieved successfully",
  "data": {
    "id": 50,
    "documentName": "curriculum_proposal_v1_trk100_step1.pdf",
    "originalFilename": "curriculum_proposal.pdf",
    "documentType": "CURRICULUM_PROPOSAL",
    "documentTypeDisplayName": "Curriculum Proposal",
    "filePath": "tracking/100/step/1/curriculum_proposal_v1_trk100_step1.pdf",
    "fileSize": 204800,
    "formattedFileSize": "200 KB",
    "contentType": "application/pdf",
    "fileExtension": "pdf",
    "description": "Initial curriculum proposal document",
    "uploadedByName": "Jane Smith",
    "versionNumber": 1,
    "uploadedAt": "2025-05-31T10:05:00",
    "isActive": true
  }
}
```

---

### GET `/api/v1/tracking/documents/tracking/{trackingId}`
Get all documents attached to a tracking record.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Tracking documents retrieved successfully",
  "data": [ { ... }, { ... } ]
}
```

---

### GET `/api/v1/tracking/documents/step/{stepId}`
Get all documents attached to a specific tracking step.

**Auth required:** No

**Path parameter:** `stepId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Step documents retrieved successfully",
  "data": [ { ... } ]
}
```

---

### GET `/api/v1/tracking/documents/tracking/{trackingId}/type/{documentType}`
Get documents of a specific type within a tracking.

**Auth required:** No

**Path parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `trackingId` | Long | Tracking record ID |
| `documentType` | String | Document type enum value |

**Example:**
```
GET /api/v1/tracking/documents/tracking/100/type/CURRICULUM_PROPOSAL
```

**Success response `200 OK`:**
```json
{
  "message": "Documents by type retrieved successfully",
  "data": [ { ... } ]
}
```

---

### GET `/api/v1/tracking/documents/search`
Search documents by name or description.

**Auth required:** No

**Query parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| `searchTerm` | Yes | Text to search for |
| `trackingId` | No | Scope search to a specific tracking |

**Example:**
```
GET /api/v1/tracking/documents/search?searchTerm=proposal&trackingId=100
```

**Success response `200 OK`:**
```json
{
  "message": "Document search completed successfully",
  "data": [ { ... } ]
}
```

---

### PUT `/api/v1/tracking/documents/{documentId}`
Update a document's description and/or type.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Path parameter:** `documentId` (Long)

**Query parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| `description` | No | New description |
| `documentType` | No | New document type enum value |

**Example:**
```
PUT /api/v1/tracking/documents/50?description=Updated+proposal&documentType=REVISION_DOCUMENTS
```

**Success response `200 OK`:**
```json
{
  "message": "Document metadata updated successfully",
  "data": { ... }
}
```

---

### DELETE `/api/v1/tracking/documents/{documentId}`
Delete a document.

**Auth required:** Yes — `ADMIN` or `QA`

**Path parameter:** `documentId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Document deleted successfully",
  "data": {
    "deleted": true
  }
}
```

---

### POST `/api/v1/tracking/documents/{documentId}/version`
Upload a new version of an existing document.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Content-Type:** `multipart/form-data`

**Path parameter:** `documentId` (Long)

**Form fields:**
| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `file` | Yes | File | New version of the file |
| `description` | No | String | Version notes |

**Success response `201 Created`:**
```json
{
  "message": "Document version created successfully",
  "data": {
    "id": 51,
    "versionNumber": 2,
    "originalFilename": "curriculum_proposal_v2.pdf",
    ...
  }
}
```

---

### GET `/api/v1/tracking/documents/versions`
Get all versions of a document identified by its name and tracking.

**Auth required:** No

**Query parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| `documentName` | Yes | Base document name |
| `trackingId` | Yes | Tracking record ID |

**Example:**
```
GET /api/v1/tracking/documents/versions?documentName=curriculum_proposal&trackingId=100
```

**Success response `200 OK`:**
```json
{
  "message": "Document versions retrieved successfully",
  "data": [ { ... }, { ... } ]
}
```

---

### GET `/api/v1/tracking/documents/statistics`
Get storage usage statistics.

**Auth required:** Yes — `ADMIN` or `QA`

**Query parameter:** `trackingId` (optional — scopes stats to a single tracking)

**Success response `200 OK`:**
```json
{
  "message": "Storage statistics retrieved successfully",
  "data": {
    "totalDocuments": 120,
    "totalSize": 52428800,
    "formattedTotalSize": "50 MB"
  }
}
```

---

### POST `/api/v1/tracking/documents/{documentId}/copy`
Copy a document to a different tracking step.

**Auth required:** Yes — `QA` or `DEAN`

**Path parameter:** `documentId` (Long)

**Query parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| `targetTrackingId` | Yes | Target tracking ID |
| `targetStepId` | Yes | Target step ID |

**Example:**
```
POST /api/v1/tracking/documents/50/copy?targetTrackingId=101&targetStepId=15
```

**Success response `201 Created`:**
```json
{
  "message": "Document copied successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/documents/upload-url`
Get a pre-signed URL for direct-to-storage file upload.

**Auth required:** Yes — `QA`, `DEAN`, or `HOD`

**Query parameters:**
| Parameter | Required | Description |
|-----------|----------|-------------|
| `trackingId` | Yes | Tracking record ID |
| `stepId` | Yes | Step ID |
| `fileName` | Yes | File name to use |
| `contentType` | Yes | MIME type (e.g. `application/pdf`) |
| `expirationMinutes` | No | Default `60` |

**Example:**
```
GET /api/v1/tracking/documents/upload-url?trackingId=100&stepId=1&fileName=proposal.pdf&contentType=application/pdf
```

**Success response `200 OK`:**
```json
{
  "message": "Upload URL generated successfully",
  "data": {
    "uploadUrl": "https://storage.example.com/presigned-upload...",
    "expiresInMinutes": 60,
    "fileName": "proposal.pdf"
  }
}
```

---

## 12. Tracking Steps

Base path: `/api/v1/tracking/steps`

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}`
Get all steps for a tracking record, paginated.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps retrieved successfully",
  "data": {
    "steps": [
      {
        "id": 1,
        "stage": "IDEATION",
        "stageDisplayName": "Curriculum Ideation",
        "action": "INITIATE",
        "actionDisplayName": "Initiate Tracking",
        "performedByName": "Jane Smith",
        "performedByEmail": "jane.smith@university.ac.ke",
        "assignedToName": "Jane Smith",
        "assignedToEmail": "jane.smith@university.ac.ke",
        "fromStage": null,
        "toStage": null,
        "notes": "New curriculum proposal for data science",
        "performedAt": "2025-05-31T10:00:00",
        "dueDate": null,
        "isMilestone": true,
        "isStageTransition": true,
        "isForwardMovement": true,
        "isBackwardMovement": false,
        "documents": []
      }
    ],
    "currentPage": 0,
    "totalPages": 1,
    "totalElements": 1,
    "pageSize": 20,
    "hasNext": false,
    "hasPrevious": false
  }
}
```

---

### GET `/api/v1/tracking/steps/{stepId}`
Get a specific tracking step by its ID.

**Auth required:** No

**Path parameter:** `stepId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Tracking step retrieved successfully",
  "data": { ... }
}
```

---

### POST `/api/v1/tracking/steps/search`
Search tracking steps with filter criteria.

**Auth required:** No

**Request body:** (all fields optional)
```json
{
  "trackingId": 100,
  "stage": "REVIEW_APPROVAL",
  "action": "APPROVE",
  "performedByUserId": 3,
  "assignedToUserId": 5,
  "performedAfter": "2025-01-01T00:00:00",
  "performedBefore": "2025-12-31T23:59:59",
  "isMilestone": true,
  "isStageTransition": false
}
```

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps search completed successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/stage/{stage}`
Get steps in a specific stage for a tracking.

**Auth required:** No

**Path parameters:** `trackingId` (Long), `stage` (String)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by stage retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/action/{action}`
Get steps of a specific action type for a tracking.

**Auth required:** No

**Path parameters:** `trackingId` (Long), `action` (String)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by action retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/user/{userId}/performed`
Get steps performed by a specific user.

**Auth required:** No

**Path parameter:** `userId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "User tracking steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/user/{userId}/assigned`
Get steps assigned to a specific user.

**Auth required:** No

**Path parameter:** `userId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Assigned tracking steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/milestones`
Get all milestone steps for a tracking.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Milestone tracking steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/transitions`
Get all stage transition steps for a tracking.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Stage transition steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/recent`
Get the N most recent steps for a tracking.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Query parameter:** `limit` (default `10`, min `1`, max `50`)

**Example:**
```
GET /api/v1/tracking/steps/tracking/100/recent?limit=5
```

**Success response `200 OK`:**
```json
{
  "message": "Recent tracking steps retrieved successfully",
  "data": [ { ... }, { ... } ]
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/latest`
Get the single most recent step for a tracking.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Success response `200 OK`:**
```json
{
  "message": "Latest tracking step retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/tracking/{trackingId}/date-range`
Get steps within a date range for a specific tracking.

**Auth required:** No

**Path parameter:** `trackingId` (Long)

**Query parameters:**
| Parameter | Required | Format |
|-----------|----------|--------|
| `startDate` | Yes | ISO-8601: `2025-01-01T00:00:00` |
| `endDate` | Yes | ISO-8601: `2025-06-30T23:59:59` |

**Example:**
```
GET /api/v1/tracking/steps/tracking/100/date-range?startDate=2025-01-01T00:00:00&endDate=2025-06-30T23:59:59
```

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by date range retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/stage/{stage}`
Get all steps globally filtered by stage.

**Auth required:** No

**Path parameter:** `stage` (String)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by stage retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/action/{action}`
Get all steps globally filtered by action.

**Auth required:** No

**Path parameter:** `action` (String)

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by action retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/milestones`
Get all milestone steps across all trackings.

**Auth required:** No

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "All milestone steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/transitions`
Get all stage transition steps across all trackings.

**Auth required:** No

**Query parameters:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "All stage transition steps retrieved successfully",
  "data": { ... }
}
```

---

### GET `/api/v1/tracking/steps/date-range`
Get all steps globally within a date range.

**Auth required:** No

**Query parameters:**
| Parameter | Required | Format |
|-----------|----------|--------|
| `startDate` | Yes | ISO-8601 |
| `endDate` | Yes | ISO-8601 |

**Query parameters also accept:** Standard pagination params.

**Success response `200 OK`:**
```json
{
  "message": "Tracking steps by date range retrieved successfully",
  "data": { ... }
}
```

---

## 13. Enums Reference

### `TrackingStage`
Defines the stage within the curriculum approval workflow.

| Value | Display Name |
|-------|-------------|
| `IDEATION` | Curriculum Ideation |
| `REVIEW_APPROVAL` | Review & Tracking Approval |
| `SCHOOL_BOARD` | School Board Review |
| `DEAN_COMMITTEE` | Dean's Committee Review |
| `SENATE` | Senate Review |
| `QA_INTERNAL_AUDIT` | QA Internal Audit |
| `CUE_EXTERNAL_AUDIT` | CUE External Audit |
| `VICE_CHANCELLOR_APPROVAL` | Vice Chancellor Approval |
| `ACCREDITED` | Tracking Completed |

**Stage progression (forward):**
`IDEATION` → `REVIEW_APPROVAL` → `SCHOOL_BOARD` → `DEAN_COMMITTEE` → `SENATE` → `QA_INTERNAL_AUDIT` → `CUE_EXTERNAL_AUDIT` → `VICE_CHANCELLOR_APPROVAL` → `ACCREDITED`

---

### `TrackingStatus`
Overall status of a tracking record.

| Value | Display Name |
|-------|-------------|
| `INITIATED` | Initiated |
| `IN_PROGRESS` | In Progress |
| `APPROVED` | Approved |
| `REJECTED` | Rejected |
| `RETURNED_FOR_REVISION` | Returned for Revision |
| `COMPLETED` | Completed |

---

### `TrackingAction`
Actions that can be performed on a tracking record.

| Value | Display Name |
|-------|-------------|
| `INITIATE` | Initiate Tracking |
| `APPROVE` | Approve |
| `REJECT` | Reject |
| `RETURN` | Return for Revision |
| `SUBMIT` | Submit |
| `REVIEW` | Review |
| `COMPLETE` | Complete |

---

### `CurriculumStatus`
Status of a curriculum record.

| Value |
|-------|
| `PENDING` |
| `APPROVED` |
| `REJECTED` |
| `UNDER_REVIEW` |

---

### `DocumentType`
Types of documents in the system.

| Value | Display Name |
|-------|-------------|
| `CURRICULUM_PROPOSAL` | Curriculum Proposal |
| `SUPPORTING_DOCUMENTS` | Supporting Documents |
| `REVISION_DOCUMENTS` | Revision Documents |
| `APPROVAL_CERTIFICATE` | Approval Certificate |
| `AUDIT_REPORT` | Audit Report |
| `OTHER` | Other |

---

### Roles
| Role name | Used in |
|-----------|---------|
| `ADMIN` | Full system administration |
| `DEAN` | School-level review and approvals |
| `QA` | Quality assurance tracking management |
| `HOD` | Head of Department — can initiate tracking |
| `VICE_CHANCELLOR` | Read access; final approver stage |
