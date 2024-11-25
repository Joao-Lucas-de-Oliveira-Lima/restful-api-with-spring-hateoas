package edu.jl.springhateoas.service;

import edu.jl.springhateoas.dto.user.UserRequestDto;
import edu.jl.springhateoas.dto.user.UserResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;

import java.util.UUID;

public interface UserService {
    UserResponseDto findById(UUID id, Boolean hateoasEnabled);
    UserResponseDto save(UserRequestDto userRequestDto, Boolean hateoasEnabled);

    CollectionModel<UserResponseDto> findAll(Boolean hateoasEnabled);
    PagedModel<UserResponseDto> findByNameContainingIgnoreCase(String name, Boolean hateoasEnabled, Pageable pageable);

    UserResponseDto update(UUID id, UserRequestDto userUpdate, Boolean hateoasEnabled);

    void delete(UUID id);
}
