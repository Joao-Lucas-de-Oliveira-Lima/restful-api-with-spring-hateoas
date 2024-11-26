package edu.jl.springhateoas.mock;

import edu.jl.springhateoas.dto.user.UserRequestDto;
import edu.jl.springhateoas.dto.user.UserResponseDto;
import edu.jl.springhateoas.entity.UserEntity;
import org.springframework.hateoas.Links;

import java.util.Map;
import java.util.UUID;

public class UserMock {
    protected int totalUsersInDatabase = 20;

    protected UserResponseDto userResponse = new UserResponseDto(
            UUID.fromString("1e1f3e26-9b01-4d7d-a123-123456789001"), "Alice", 25);

    protected UserRequestDto validUserRequest = new UserRequestDto("Alice", 25);

    protected String invalidRequestNameIsNull = """
            {
                "name" : null,
                "age" : 25
            }
            """;

    protected UserRequestDto invalidRequestNameIsEmpty = new UserRequestDto("", 25);

    protected UserRequestDto invalidRequestNameIsBlank = new UserRequestDto("      ", 25);

    protected UserRequestDto invalidRequestAgeIsNull = new UserRequestDto("Alice", null);

    protected UserRequestDto invalidRequestAgeBelowMinimum = new UserRequestDto("Alice", 14);

    protected UUID nonExistentUserId = UUID.fromString("080df297-c0b1-42ce-b980-db8cc8e2afd5");

    protected record UserResponseWithNameField_Links(UUID id, String name, Integer age, Map<String, Object> _links) {}
}
