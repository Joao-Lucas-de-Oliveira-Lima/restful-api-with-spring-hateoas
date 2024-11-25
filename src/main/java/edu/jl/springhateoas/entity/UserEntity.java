package edu.jl.springhateoas.entity;

import jakarta.persistence.*;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;
    private String name;
    private Integer age;

    public UserEntity() {
    }

    public UserEntity(UUID id, String name, Integer age) {
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
        UserEntity userEntity = (UserEntity) object;
        return Objects.equals(id, userEntity.id) && Objects.equals(name, userEntity.name) && Objects.equals(age, userEntity.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, age);
    }
}
