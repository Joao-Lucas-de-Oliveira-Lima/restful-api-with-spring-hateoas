package edu.jl.springhateoas.dto.user;

import org.apache.catalina.User;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UserResponseDto extends RepresentationModel<UserResponseDto> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;
    private Integer age;

    public UserResponseDto(){}

    public UserResponseDto(UUID id, String name, Integer age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public UserResponseDto(Link initialLink, UUID id, String name, Integer age) {
        super(initialLink);
        this.id = id;
        this.name = name;
        this.age = age;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;
        UserResponseDto that = (UserResponseDto) object;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, name, age);
    }
}
