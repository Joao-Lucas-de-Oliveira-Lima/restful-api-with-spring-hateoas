package edu.jl.springhateoas.service.implementation;

import edu.jl.springhateoas.controller.UserController;
import edu.jl.springhateoas.dto.user.UserRequestDto;
import edu.jl.springhateoas.dto.user.UserResponseDto;
import edu.jl.springhateoas.entity.UserEntity;
import edu.jl.springhateoas.exception.ResourceNotFoundException;
import edu.jl.springhateoas.mapper.Mapper;
import edu.jl.springhateoas.repository.UserRepository;
import edu.jl.springhateoas.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Service
public class UserServiceImplementation implements UserService {
    private final Mapper mapper;
    private final UserRepository userRepository;

    @Autowired
    public UserServiceImplementation(Mapper mapper, UserRepository userRepository) {
        this.mapper = mapper;
        this.userRepository = userRepository;
    }

    public UserResponseDto findById(UUID id, Boolean hateoasEnabled) {
        UserEntity userFound = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        UserResponseDto userFoundAsResponseDto = mapper.convertObject(userFound, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(userFoundAsResponseDto);
        }
        return userFoundAsResponseDto;
    }

    @Override
    public UserResponseDto save(UserRequestDto userRequestDto, Boolean hateoasEnabled) {
        UserEntity userSaved = userRepository.save(mapper.convertObject(userRequestDto, UserEntity.class));
        UserResponseDto userSavedAsResponseDto = mapper.convertObject(userSaved, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(userSavedAsResponseDto);
        }
        return userSavedAsResponseDto;
    }

    @Override
    public CollectionModel<UserResponseDto> findAll(Boolean hateoasEnabled) {
        List<UserEntity> usersFound = userRepository.findAll();
        List<UserResponseDto> responseDtoList;
        if (!hateoasEnabled) {
            responseDtoList = mapper.convertList(usersFound, UserResponseDto.class);
            return CollectionModel.of(responseDtoList);
        }
        responseDtoList = usersFound.stream().map(user -> mapper.convertObject(user, UserResponseDto.class)).map(this::applyLinks).toList();
        return CollectionModel.of(
                responseDtoList,
                linkTo(methodOn(UserController.class).findAll(true)).withSelfRel()
        );
    }

    @Override
    public PagedModel<UserResponseDto> findByNameContainingIgnoreCase(String name, Boolean hateoasEnabled, Pageable pageable) {
        Page<UserEntity> userEntityPage = userRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<UserResponseDto> content;
        if (hateoasEnabled) {
            content = userEntityPage.map(userEntity -> mapper.convertObject(userEntity, UserResponseDto.class)).map(this::applyLinks);
            return applyPageLinks(content);
        }
        content = userEntityPage.map(userEntity -> mapper.convertObject(userEntity, UserResponseDto.class));
        return PagedModel.of(
                content.getContent(),
                new PagedModel.PageMetadata(content.getSize(), content.getNumber(), content.getTotalElements(), content.getTotalPages())
        );
    }

    @Override
    public UserResponseDto update(UUID id, UserRequestDto userUpdate, Boolean hateoasEnabled) throws ResourceNotFoundException {
        UserEntity userFound = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        mapper.copyProperties(userUpdate, userFound);
        UserEntity updatedUser = userRepository.save(userFound);
        UserResponseDto updatedUserAsResponseDto = mapper.convertObject(updatedUser, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(updatedUserAsResponseDto);
        }
        return updatedUserAsResponseDto;
    }

    @Override
    public void delete(UUID id) {
        UserEntity userFound = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        userRepository.delete(userFound);
    }

    private UserResponseDto applyLinks(UserResponseDto userResponseDto) {
        try {
            userResponseDto.add(linkTo(methodOn(UserController.class).findById(userResponseDto.getId(), true)).withSelfRel());
            userResponseDto.add(linkTo(methodOn(UserController.class).save(new UserRequestDto("John Doe", 25), true, new BeanPropertyBindingResult(userResponseDto, "UserResponseDto"))).withRel("create"));
            userResponseDto.add(linkTo(methodOn(UserController.class).delete(userResponseDto.getId())).withRel("delete"));
            userResponseDto.add(linkTo(methodOn(UserController.class).update(UUID.randomUUID(), new UserRequestDto("John Doe", 25), true, new BeanPropertyBindingResult(userResponseDto, "UserResponseDto"))).withRel("update"));
            return userResponseDto;
        } catch (Exception exception) {
            throw new RuntimeException("Links could not be added to object " + userResponseDto.getClass().getName() + "!");
        }

    }

    private PagedModel<UserResponseDto> applyPageLinks(Page<UserResponseDto> page) {
        int size = page.getSize(), number = page.getNumber();
        long totalElements = page.getTotalElements(), totalPages = page.getTotalPages();
        List<Link> links = new ArrayList<>();
        String uri = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
        // self
        links.add(Link.of(uri).withSelfRel());
        // first
        String uriFirstPage = uri.replaceAll("\\bpage=" + number + "\\b|\\bpage\\b", "page=0");
        links.add(Link.of(uriFirstPage).withRel("first"));
        // last
        String uriLastPage;
        if (totalPages == 0) {
            uriLastPage = uri.replaceAll("\\bpage=" + number + "\\b|\\bpage\\b", "page=" + (totalPages));
        } else {
            uriLastPage = uri.replaceAll("\\bpage=" + number + "\\b|\\bpage\\b", "page=" + (totalPages - 1));
        }
        links.add(Link.of(uriLastPage).withRel("last"));
        // next
        String uriNextPage;
        if (totalPages > 1 && number < totalPages - 1) {
            uriNextPage = uri.replaceAll("\\bpage=" + number + "\\b|\\bpage\\b", "page=" + (number + 1));
            links.add(Link.of(uriNextPage).withRel("next"));
        }
        // prev
        String uriPrevPage;
        if (number > 1) {
            uriPrevPage = uri.replaceAll("\\bpage=" + number + "\\b|\\bpage\\b", "page=" + (number + -1));
            links.add(Link.of(uriPrevPage).withRel("prev"));

        }
        return PagedModel.of(
                page.getContent(),
                new PagedModel.PageMetadata(size, number, totalElements, totalPages),
                links
        );
    }
}
