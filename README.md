# RESTful API with Spring HATEOAS

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![Swagger](https://img.shields.io/badge/-Swagger-%23Clojure?style=for-the-badge&logo=swagger&logoColor=white)

## Table of Contents

- [Overview](#overview)
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Example Responses](#example-responses)
- [Testing](#testing)
- [API Documentation](#api-documentation)

## Overview

This API implements Level 3 of the Richardson REST Maturity Model using HATEOAS links. By default, HATEOAS links are disabled but can be enabled via a query parameter (`hateoas=true`).

## Installation

### Prerequisites
- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)

### Running the Application
```bash
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080/api`.

## Usage

To enable HATEOAS links, append `hateoas=true` as a query parameter.

### API Endpoints

| Method | Endpoint | Description |
|--------|---------|-------------|
| GET | `/api/v1/users/:id?hateoas=true` | Retrieve a user by ID |
| GET | `/api/v1/users?hateoas=true` | List all users |
| GET | `/api/v1/users/paged?hateoas=true` | Get paginated user data |

## Example Responses

### Single User Response
```json
{
  "id": "1e1f3e26-9b01-4d7d-a123-123456789001",
  "name": "Alice",
  "age": 25,
  "_links": {
    "self": { "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001?hateoas=true" },
    "create": { "href": "http://localhost:8080/api/v1/users?hateoas=true" },
    "delete": { "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001" },
    "update": { "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001?hateoas=true" }
  }
}
```

### Paginated Users Response
```json
{
  "_embedded": {
    "userResponseDtoList": [
      {
        "id": "1e1f3e26-9b01-4d7d-a123-123456789001",
        "name": "Alice",
        "age": 25,
        "_links": { /* User-specific links */ }
      }
    ]
  },
  "_links": {
    "self": { "href": "http://localhost:8080/api/v1/users/paged?size=20&page=0&hateoas=true" },
    "first": { "href": "http://localhost:8080/api/v1/users/paged?size=20&page=0&hateoas=true" },
    "last": { "href": "http://localhost:8080/api/v1/users/paged?size=20&page=0&hateoas=true" }
  },
  "page": {
    "size": 20,
    "totalElements": 20,
    "totalPages": 1,
    "number": 0
  }
}
```

## Testing

Run unit and integration tests:

- **Unit Tests:**
```bash
./mvnw test
```
- **Integration Tests:**
```bash
./mvnw verify
```

## API Documentation

- **Swagger UI:** Access interactive API documentation at [`/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html).
- **OpenAPI Specification (JSON):** Retrieve the API spec at [`/v3/api-docs`](http://localhost:8080/v3/api-docs).

---
