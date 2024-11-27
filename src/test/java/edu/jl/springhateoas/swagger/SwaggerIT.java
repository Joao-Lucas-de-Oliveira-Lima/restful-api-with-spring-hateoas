package edu.jl.springhateoas.swagger;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;

/**
 * Integration tests for Swagger UI and API documentation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SwaggerIT {

    @LocalServerPort
    private int localPort;

    @BeforeEach
    public void setUpRestAssuredPort() {
        RestAssured.port = localPort;
    }

    @Test
    @DisplayName("Should successfully load the Swagger UI")
    public void shouldLoadSwaggerUI() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/swagger-ui/index.html")
                .then()
                .statusCode(200)
                .contentType(ContentType.HTML);
    }

    @Test
    @DisplayName("Should successfully load the API documentation")
    public void shouldLoadApiDocumentation() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/v3/api-docs")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }
}
