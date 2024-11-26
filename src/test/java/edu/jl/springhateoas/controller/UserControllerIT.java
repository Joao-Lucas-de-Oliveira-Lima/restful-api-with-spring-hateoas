package edu.jl.springhateoas.controller;

import edu.jl.springhateoas.dto.user.UserResponseDto;
import edu.jl.springhateoas.mock.UserMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@link UserController}
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIT extends UserMock {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnNotFoundWhenUserDoesNotExist() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/{id}", nonExistentUserId.toString())
                .then()
                .statusCode(404)
                .body("timestamp", notNullValue())
                .body("details", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Should return user successfully without HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnUserWithoutHateoas() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(userResponse.getId().toString()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .body("$", not(hasKey("_links")));  // Verifying that the "_links" field does not exist
    }

    @Test
    @DisplayName("Should return user successfully with HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnUserWithHateoas() {
        HashMap<String, String> links = createUserResponseLinks(userResponse.getId());
        given()
                .contentType(ContentType.JSON)
                .param("hateoas", true)
                .when()
                .get("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(userResponse.getId().toString()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .body("_links.self.href", equalTo(links.get("self")))
                .body("_links.create.href", equalTo(links.get("create")))
                .body("_links.update.href", equalTo(links.get("update")))
                .body("_links.delete.href", equalTo(links.get("delete")));
    }

    @Test
    @DisplayName("Should return a list of users successfully with HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnListOfUsersWithHateoasLinks() {
        Response response = given()
                .contentType(ContentType.JSON)
                .param("hateoas", true)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(200)
                .body("_embedded.userResponseDtoList", hasSize(20))
                .body("_links.self.href", equalTo(RestAssured.baseURI + ":" + port + "/api/v1/users?hateoas=true"))
                .extract().response();

        List<UserResponseWithNameField_Links> userList = response.jsonPath().getList("_embedded.userResponseDtoList", UserResponseWithNameField_Links.class);

        userList.forEach(userDto -> {
            HashMap<String, String> links = createUserResponseLinks(userDto.id());
            assertThat(userDto._links().get("self").toString()).isEqualTo("{href=" + links.get("self") + "}");
            assertThat(userDto._links().get("create").toString()).isEqualTo("{href=" + links.get("create") + "}");
            assertThat(userDto._links().get("update").toString()).isEqualTo("{href=" + links.get("update") + "}");
            assertThat(userDto._links().get("delete").toString()).isEqualTo("{href=" + links.get("delete") + "}");
        });
    }

    @Test
    @DisplayName("Should return users without HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnUsersWithoutLinks() {
        Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/v1/users")
                .then()
                .statusCode(200)
                .body("_embedded.userResponseDtoList", hasSize(20))
                .body("_embedded", not(hasKey("_links")))
                .extract().response();

        List<UserResponseDto> userList = response.jsonPath().getList("_embedded.userResponseDtoList", UserResponseDto.class);
        userList.forEach(userDto -> {
            assertThat(userDto.getLinks()).hasSize(0);
        });
    }

    @DisplayName("Should return a paginated list of users with appropriate page and user links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @RepeatedTest(value = 10)
    void shouldReturnPaginatedUsersWithLinks(RepetitionInfo repetitionInfo) {
        String name = "";
        int size = 2, page = repetitionInfo.getCurrentRepetition() - 1;
        String sort = "name,asc";
        int totalPages = (int) Math.ceil((double) totalUsersInDatabase / size);

        HashMap<String, String> pageLinks = createPageLinks(name, size, page, totalPages);

        Response response = given()
                .contentType(ContentType.JSON)
                .param("name", name)
                .param("size", size)
                .param("page", page)
                .param("sort", sort)
                .param("hateoas", true)
                .when()
                .get("/api/v1/users/paged")
                .then()
                .statusCode(200)
                .body("_embedded.userResponseDtoList", hasSize(2))
                .body("_links.self.href", equalTo(pageLinks.get("self") + "&hateoas=true"))
                .body("_links.first.href", equalTo(pageLinks.get("first") + "&hateoas=true"))
                .body("_links.last.href", equalTo(pageLinks.get("last") + "&hateoas=true"))
                .extract().response();

        if (page == totalPages - 1) {
            response.then().body("_links", not(hasKey("next")));
        } else {
            response.then().body("_links.next.href", equalTo(pageLinks.get("next") + "&hateoas=true"));
        }

        if (page == 0) {
            response.then().body("_links", not(hasKey("prev")));
        } else {
            response.then().body("_links.prev.href", equalTo(pageLinks.get("prev") + "&hateoas=true"));
        }

        List<UserResponseWithNameField_Links> userList = response.jsonPath().getList("_embedded.userResponseDtoList", UserResponseWithNameField_Links.class);
        userList.forEach(userDto -> {
            HashMap<String, String> userLinks = createUserResponseLinks(userDto.id());
            assertThat(userDto._links().get("self").toString()).isEqualTo("{href=" + userLinks.get("self") + "}");
            assertThat(userDto._links().get("create").toString()).isEqualTo("{href=" + userLinks.get("create") + "}");
            assertThat(userDto._links().get("update").toString()).isEqualTo("{href=" + userLinks.get("update") + "}");
            assertThat(userDto._links().get("delete").toString()).isEqualTo("{href=" + userLinks.get("delete") + "}");
        });
    }

    @DisplayName("Should return a paginated list of users without HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @RepeatedTest(value = 10)
    void shouldReturnPaginatedUsersWithoutLinks(RepetitionInfo repetitionInfo) {
        String name = "";
        int size = 2, page = repetitionInfo.getCurrentRepetition() - 1;
        String sort = "name,asc";

        given()
                .contentType(ContentType.JSON)
                .param("name", name)
                .param("size", size)
                .param("page", page)
                .param("sort", sort)
                .when()
                .get("/api/v1/users/paged")
                .then()
                .statusCode(200)
                .body("_embedded.userResponseDtoList", hasSize(2))
                .body("$", not(hasKey("_links")))
                .body("_embedded.userResponseDtoList[0]", not(hasKey("_links")))
                .body("_embedded.userResponseDtoList[1]", not(hasKey("_links")));
    }

    @Test
    @DisplayName("Should successfully save a user and generate HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSaveUserWithHateoasLinks() {
        Response response = given()
                .contentType(ContentType.JSON)
                .body(validUserRequest)
                .when()
                .post("/api/v1/users?hateoas=true")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .extract().response();

        UserResponseWithNameField_Links userResponseDtoWithLinks = response.jsonPath().getObject("$", UserResponseWithNameField_Links.class);
        HashMap<String, String> links = createUserResponseLinks(userResponseDtoWithLinks.id());
        response.then().body("_links.self.href", equalTo(links.get("self")));
        response.then().body("_links.create.href", equalTo(links.get("create")));
        response.then().body("_links.update.href", equalTo(links.get("update")));
        response.then().body("_links.delete.href", equalTo(links.get("delete")));
    }

    @Test
    @DisplayName("Should successfully save a user without generating HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldSaveUserWithoutHateoasLinks() {
        given()
                .contentType(ContentType.JSON)
                .body(validUserRequest)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(201)
                .body("id", is(notNullValue()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .body("$", not(hasKey("_links")));
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to save a user with 'name' being null")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenSavingUserWithNameNull() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsNull)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to save a user with 'name' being empty")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenSavingUserWithNameEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsEmpty)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to save a user with 'name' being blank")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenSavingUserWithNameBlank() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsBlank)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to save a user with 'age' being null")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenSavingUserWithAgeNull() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestAgeIsNull)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to save a user with 'age' being below the minimum allowed")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenSavingUserWithAgeBelowMinimum() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestAgeBelowMinimum)
                .when()
                .post("/api/v1/users")
                .then()
                .statusCode(400);
    }


    @Test
    @DisplayName("Should successfully update a user and retrieve the associated HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateUserWithHateoasLinks() {
        HashMap<String, String> links = createUserResponseLinks(userResponse.getId());
        given()
                .contentType(ContentType.JSON)
                .param("hateoas", true)
                .body(validUserRequest)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(userResponse.getId().toString()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .body("_links.self.href", equalTo(links.get("self")))
                .body("_links.create.href", equalTo(links.get("create")))
                .body("_links.update.href", equalTo(links.get("update")))
                .body("_links.delete.href", equalTo(links.get("delete")));
    }

    @Test
    @DisplayName("Should successfully update a user without retrieving HATEOAS links")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldUpdateUserWithoutHateoasLinks() {
        given()
                .contentType(ContentType.JSON)
                .body(validUserRequest)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(200)
                .body("id", equalTo(userResponse.getId().toString()))
                .body("age", equalTo(userResponse.getAge()))
                .body("name", equalTo(userResponse.getName()))
                .body("$", not(hasKey("_links")));
    }

    @Test
    @DisplayName("Should return 404 Not Found due to invalid user ID")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnNotFoundWhenUserIdIsInvalid() {
        given()
                .contentType(ContentType.JSON)
                .body(validUserRequest)
                .when()
                .put("/api/v1/users/{id}", nonExistentUserId)
                .then()
                .statusCode(404)
                .body("timestamp", notNullValue())
                .body("details", notNullValue())
                .body("message", notNullValue());
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to update a user with 'name' being null")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdatingUserWithNameNull() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsNull)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to update a user with 'name' being empty")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdatingUserWithNameEmpty() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsEmpty)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to update a user with 'name' being blank")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdatingUserWithNameBlank() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestNameIsBlank)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to update a user with 'age' being null")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdatingUserWithAgeNull() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestAgeIsNull)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should return 400 BadRequest when trying to update a user with 'age' being below the minimum allowed")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnBadRequestWhenUpdatingUserWithAgeBelowMinimum() {
        given()
                .contentType(ContentType.JSON)
                .body(invalidRequestAgeBelowMinimum)
                .when()
                .put("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("Should successfully delete a user returning an empty body")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteUserWithoutHateoasLinks() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
    }

    @Test
    @DisplayName("Should successfully delete a user with requesting HATEOAS links and return an empty body")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldDeleteUserWithHateoasLinks() {
        given()
                .contentType(ContentType.JSON)
                .param("hateoas", true)
                .when()
                .delete("/api/v1/users/{id}", userResponse.getId())
                .then()
                .statusCode(204)
                .body(is(emptyOrNullString()));
    }

    @Test
    @DisplayName("Should return 404 Not Found due to user not existing")
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void shouldReturnNotFoundWhenUserDoesNotExistOnDelete() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/v1/users/{id}", nonExistentUserId)
                .then()
                .statusCode(404)
                .body("timestamp", notNullValue())
                .body("details", notNullValue())
                .body("message", notNullValue());
    }

    private HashMap<String, String> createUserResponseLinks(UUID userId) {
        String baseUri = RestAssured.baseURI + ":" + port + "/api/v1/users";
        String hateoasRequestParam = "?hateoas=true";
        HashMap<String, String> links = new HashMap<>();
        links.put("self", baseUri + "/" + userId + hateoasRequestParam);
        links.put("update", baseUri + "/" + userId + hateoasRequestParam);
        links.put("delete", baseUri + "/" + userId);
        links.put("create", baseUri + hateoasRequestParam);
        return links;
    }

    private HashMap<String, String> createPageLinks(String name, int size, int page, int totalPages) {
        HashMap<String, String> links = new HashMap<>();
        String uri = RestAssured.baseURI + ":" + port + "/api/v1/users/paged";

        links.put("self", uri + "?name=" + name + "&size=" + size + "&page=" + page + "&sort=name%252Casc");
        links.put("first", uri + "?name=" + name + "&size=" + size + "&page=0&sort=name%252Casc");
        links.put("last", uri + "?name=" + name + "&size=" + size + "&page=" + (totalPages - 1) + "&sort=name%252Casc");

        if (page < totalPages - 1) {
            links.put("next", uri + "?name=" + name + "&size=" + size + "&page=" + (page + 1) + "&sort=name%252Casc");
        }

        if (page > 0) {
            links.put("prev", uri + "?name=" + name + "&size=" + size + "&page=" + (page - 1) + "&sort=name%252Casc");
        }

        return links;
    }

}