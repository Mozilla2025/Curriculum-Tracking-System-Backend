# Curriculum Tracking System API Documentation

## Overview
This document provides comprehensive REST API endpoints for the Curriculum Tracking System. The system manages academic curriculums across schools, departments, and academic levels with role-based access control.


### Get All Deans
- **GET** `/users/deans`
- **Description**: Retrieve all users with DEAN role

### Get All Department Heads
- **GET** `/users/department-heads`
- **Description**: Retrieve all users with DEPARTMENT_HEAD role

---

## Role Management Endpoints

### Get All Roles
- **GET** `/roles`
- **Description**: Retrieve all available roles

### Get Role by ID
- **GET** `/roles/{roleId}`
- **Description**: Retrieve specific role details
- **Parameters**: 
  - `roleId` (path) - Role ID

### Get Users by Role
- **GET** `/roles/{roleId}/users`
- **Description**: Get all users assigned to a specific role
- **Parameters**: 
  - `roleId` (path) - Role ID

---

## School Management Endpoints

### Get All Schools
- **GET** `/schools`
- **Description**: Retrieve all schools

### Get School by ID
- **GET** `/schools/{schoolId}`
- **Description**: Retrieve specific school details
- **Parameters**: 
  - `schoolId` (path) - School ID

### Get School Departments
- **GET** `/schools/{schoolId}/departments`
- **Description**: Get all departments in a specific school
- **Parameters**: 
  - `schoolId` (path) - School ID

### Get School Curriculums
- **GET** `/schools/{schoolId}/curriculums`
- **Description**: Get all curriculums in a specific school
- **Parameters**: 
  - `schoolId` (path) - School ID

### Get School Curriculums by Level
- **GET** `/schools/{schoolId}/curriculums/bachelor`
- **GET** `/schools/{schoolId}/curriculums/masters`
- **GET** `/schools/{schoolId}/curriculums/phd`
- **Description**: Get curriculums by academic level in a specific school
- **Parameters**: 
  - `schoolId` (path) - School ID

---

## Department Management Endpoints

### Get All Departments
- **GET** `/departments`
- **Description**: Retrieve all departments

### Get Department by ID
- **GET** `/departments/{departmentId}`
- **Description**: Retrieve specific department details
- **Parameters**: 
  - `departmentId` (path) - Department ID

### Get Department Curriculums
- **GET** `/departments/{departmentId}/curriculums`
- **Description**: Get all curriculums in a specific department
- **Parameters**: 
  - `departmentId` (path) - Department ID

### Get Department Curriculums by Level
- **GET** `/departments/{departmentId}/curriculums/bachelor`
- **GET** `/departments/{departmentId}/curriculums/masters`
- **GET** `/departments/{departmentId}/curriculums/phd`
- **Description**: Get curriculums by academic level in a specific department
- **Parameters**: 
  - `departmentId` (path) - Department ID

---

## Academic Level Endpoints

### Get All Academic Levels
- **GET** `/academic-levels`
- **Description**: Retrieve all academic levels (Bachelor, Masters, PhD)

### Get Academic Level by ID
- **GET** `/academic-levels/{levelId}`
- **Description**: Retrieve specific academic level details
- **Parameters**: 
  - `levelId` (path) - Academic Level ID

### Get Curriculums by Academic Level
- **GET** `/academic-levels/{levelId}/curriculums`
- **Description**: Get all curriculums for a specific academic level
- **Parameters**: 
  - `levelId` (path) - Academic Level ID

---

## Curriculum Management Endpoints

### Get All Curriculums
- **GET** `/curriculums`
- **Description**: Retrieve all curriculums
- **Query Parameters**:
  - `status` - Filter by status (pending, approved, rejected, under_review)
  - `department` - Filter by department ID
  - `level` - Filter by academic level ID
  - `createdBy` - Filter by creator user ID
  - `approvedBy` - Filter by approver user ID

### Get Curriculum by ID
- **GET** `/curriculums/{curriculumId}`
- **Description**: Retrieve specific curriculum details
- **Parameters**: 
  - `curriculumId` (path) - Curriculum ID

### Get Curriculums by Academic Level
- **GET** `/curriculums/bachelor`
- **GET** `/curriculums/masters`
- **GET** `/curriculums/phd`
- **Description**: Get all curriculums by academic level

### Get Curriculum Details
- **GET** `/curriculums/{curriculumId}/details`
- **Description**: Get all details for a specific curriculum
- **Parameters**: 
  - `curriculumId` (path) - Curriculum ID

### Get Curriculum Comments
- **GET** `/curriculums/{curriculumId}/comments`
- **Description**: Get all comments for a specific curriculum
- **Parameters**: 
  - `curriculumId` (path) - Curriculum ID

---

## Status-based Curriculum Endpoints

### Get Curriculums by Status
- **GET** `/curriculums?status=pending`
- **GET** `/curriculums?status=approved`
- **GET** `/curriculums?status=rejected`
- **GET** `/curriculums?status=under_review`
- **Description**: Get curriculums filtered by approval status

### Get Curriculums by Level and Status
- **GET** `/curriculums/bachelor?status=approved`
- **GET** `/curriculums/masters?status=pending`
- **GET** `/curriculums/phd?status=rejected`
- **Description**: Get curriculums filtered by both academic level and status

---

## Dean-specific Endpoints

### Get Dean's Schools
- **GET** `/deans/{deanId}/schools`
- **Description**: Get all schools under a specific dean
- **Parameters**: 
  - `deanId` (path) - Dean User ID

### Get Dean's Curriculums
- **GET** `/deans/{deanId}/curriculums`
- **Description**: Get all curriculums under a dean's jurisdiction
- **Parameters**: 
  - `deanId` (path) - Dean User ID
- **Query Parameters**:
  - `status` - Filter by status
  - `level` - Filter by academic level

### Get Dean's Curriculums by Level
- **GET** `/deans/{deanId}/curriculums/bachelor`
- **GET** `/deans/{deanId}/curriculums/masters`
- **GET** `/deans/{deanId}/curriculums/phd`
- **Description**: Get curriculums by academic level under a dean
- **Parameters**: 
  - `deanId` (path) - Dean User ID

### Get Dean's Curriculums by Status
- **GET** `/deans/{deanId}/curriculums?status=pending`
- **GET** `/deans/{deanId}/curriculums?status=approved`
- **Description**: Get curriculums by status under a dean
- **Parameters**: 
  - `deanId` (path) - Dean User ID

---

## Department Head Endpoints

### Get Department Head's Departments
- **GET** `/department-heads/{headId}/departments`
- **Description**: Get departments under a specific department head
- **Parameters**: 
  - `headId` (path) - Department Head User ID

### Get Department Head's Curriculums
- **GET** `/department-heads/{headId}/curriculums`
- **Description**: Get curriculums under a department head's jurisdiction
- **Parameters**: 
  - `headId` (path) - Department Head User ID

---

## Curriculum Details Endpoints

### Get All Curriculum Details
- **GET** `/curriculum-details`
- **Description**: Retrieve all curriculum details
- **Query Parameters**:
  - `type` - Filter by detail type (objective, learning_outcome, course_outline, assessment_method, resource, prerequisite)

### Get Curriculum Detail by ID
- **GET** `/curriculum-details/{detailId}`
- **Description**: Retrieve specific curriculum detail
- **Parameters**: 
  - `detailId` (path) - Curriculum Detail ID

### Get Specific Curriculum Detail
- **GET** `/curriculums/{curriculumId}/details/{detailId}`
- **Description**: Get specific detail of a curriculum
- **Parameters**: 
  - `curriculumId` (path) - Curriculum ID
  - `detailId` (path) - Detail ID

---

## Comments Endpoints

### Get All Comments
- **GET** `/comments`
- **Description**: Retrieve all comments
- **Query Parameters**:
  - `type` - Filter by comment type (general, approval, rejection, suggestion, review)

### Get Comment by ID
- **GET** `/comments/{commentId}`
- **Description**: Retrieve specific comment
- **Parameters**: 
  - `commentId` (path) - Comment ID

### Get Specific Curriculum Comment
- **GET** `/curriculums/{curriculumId}/comments/{commentId}`
- **Description**: Get specific comment of a curriculum
- **Parameters**: 
  - `curriculumId` (path) - Curriculum ID
  - `commentId` (path) - Comment ID

### Get User Comments
- **GET** `/users/{userId}/comments`
- **Description**: Get all comments made by a specific user
- **Parameters**: 
  - `userId` (path) - User ID

---

## Advanced Filtering Endpoints

### Get Curriculums in Specific School Department
- **GET** `/schools/{schoolId}/departments/{departmentId}/curriculums`
- **Description**: Get curriculums in a specific department of a specific school
- **Parameters**: 
  - `schoolId` (path) - School ID
  - `departmentId` (path) - Department ID

### Multi-parameter Curriculum Filtering
- **GET** `/schools/{schoolId}/curriculums?status=approved&level=bachelor`
- **GET** `/departments/{departmentId}/curriculums?status=pending&level=masters`
- **Description**: Get curriculums with multiple filters applied

### Get Curriculums by Creator
- **GET** `/curriculums?createdBy={userId}`
- **Description**: Get curriculums created by a specific user
- **Parameters**: 
  - `userId` (query) - Creator User ID

### Get Curriculums by Approver
- **GET** `/curriculums?approvedBy={userId}`
- **Description**: Get curriculums approved by a specific user
- **Parameters**: 
  - `userId` (query) - Approver User ID

---

## Search Endpoints

### Search Curriculums
- **GET** `/curriculums/search?q={query}`
- **Description**: Search curriculums by name or description
- **Parameters**: 
  - `q` (query) - Search query string

### Advanced Curriculum Filter
- **GET** `/curriculums?department={departmentId}&level={levelId}&status={status}`
- **Description**: Filter curriculums with multiple parameters
- **Parameters**: 
  - `department` (query) - Department ID
  - `level` (query) - Academic Level ID
  - `status` (query) - Curriculum Status

---

## Statistics Endpoints

### Get School Curriculum Statistics
- **GET** `/schools/{schoolId}/curriculums/statistics`
- **Description**: Get curriculum statistics for a specific school
- **Parameters**: 
  - `schoolId` (path) - School ID

### Get Department Curriculum Statistics
- **GET** `/departments/{departmentId}/curriculums/statistics`
- **Description**: Get curriculum statistics for a specific department
- **Parameters**: 
  - `departmentId` (path) - Department ID
