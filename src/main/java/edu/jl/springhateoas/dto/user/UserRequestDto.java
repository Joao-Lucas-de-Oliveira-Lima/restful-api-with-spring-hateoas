package edu.jl.springhateoas.dto.user;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class UserRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @NotBlank
    private String name;
    @NotNull @Min(10)
    private Integer age;

    public UserRequestDto() {
    }

    public UserRequestDto(String name, Integer age) {
        this.name = name;
        this.age = age;
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
        UserRequestDto that = (UserRequestDto) object;
        return Objects.equals(name, that.name) && Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
