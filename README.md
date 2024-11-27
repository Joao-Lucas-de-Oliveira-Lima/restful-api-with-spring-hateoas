# RESTful API with Spring HATEOAS
Implementation of a RESTful API that allows the optional inclusion of HATEOAS links through a query parameter.
## Table of Contents
- [Overview](#overview)
- [Installation Guide](#installation-guide)
- [Tests](#tests)
- [Documentation](#documentation)

## Overview
The goal of this API is to mitigate the issue of significantly larger 
JSON responses due to the inclusion of HATEOAS links, which may be unnecessary
for clients that do not need these links, such as mobile clients. This would result in 
higher mobile data consumption. To address this, the API uses a boolean query parameter 
called `hateoas`, which is set to `false` by default. When sent as `true`, the API will 
return all the links associated with the requested resource. This approach allows the API 
to be self-explanatory for clients that consume links, without burdening those that do not 
use this feature.
The API implements HATEOAS with three types of responses:
- `UserResponseDto` for a single user,
- `CollectionModel<UserResponseDto>` for a collection of users,
- `PagedModel<UserResponseDto>` for a paged list of users.
### Example
- For the request `GET http://localhost:8080/api/v1/users/:id` without the HATEOAS parameter:

```json
{
  "id": "1e1f3e26-9b01-4d7d-a123-123456789001",
  "name": "Alice",
  "age": 25
}
```
- Enabling HATEOAS with `GET http://localhost:8080/api/v1/users/:id?hateoas=true`:
```json
{
  "id": "1e1f3e26-9b01-4d7d-a123-123456789001",
  "name": "Alice",
  "age": 25,
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001?hateoas=true"
    },
    "create": {
      "href": "http://localhost:8080/api/v1/users?hateoas=true"
    },
    "delete": {
      "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001"
    },
    "update": {
      "href": "http://localhost:8080/api/v1/users/1e1f3e26-9b01-4d7d-a123-123456789001?hateoas=true"
    }
  }
}
```
>Note: The links above are just for reference. In a real software environment, 
> more relevant links tailored to the business rules of the application could be sent 
> along with the request.
## Installation Guide

### Prerequisites
- [Java 21](https://www.oracle.com/br/java/technologies/downloads/#java21)
- [Apache Maven 3.9.8 or later](https://maven.apache.org/install.html)

### Running the Application with Maven

1. **Build the Application**

In the project root directory, run the following command to start building the project:
```bash
mvn clean install
```

2. **Run the Application**

To start the application, use the command:
```bash
mvn spring-boot:run
```

## Tests

Run the following commands in the terminal, from the application root directory:

- For unit tests:
```bash
mvn test
```
- For integration tests:
```bash
mvn verify
```

## Documentation

### API Endpoints Preview
```text
GET /api/v1/users/{id} - Retrieve a user by ID.
GET /api/v1/users - Retrieve a collection of all users.
GET /api/v1/users/paged - Retrieve a paged list of users containing a case-insensitive name.
POST /api/v1/users - Create a new user.
PUT /api/v1/users/{id} - Update an existing user by ID.
DELETE /api/v1/users/{id} - Delete a user by ID.
```

### OpenAPI Documentation
- To view the full API documentation, including endpoints and data schemas, open the Swagger UI at:
  `/swagger-ui/index.html`

- For API documentation in JSON format suitable for tools like Postman, Insomnia, and other API clients, go to: `/v3/api-docs`.
