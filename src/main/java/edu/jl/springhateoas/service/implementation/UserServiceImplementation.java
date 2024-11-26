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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
        UserEntity foundUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        UserResponseDto userResponse = mapper.convertObject(foundUser, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(userResponse);
        }
        return userResponse;
    }

    @Override
    public UserResponseDto save(UserRequestDto userRequestDto, Boolean hateoasEnabled) {
        UserEntity savedUser = userRepository.save(mapper.convertObject(userRequestDto, UserEntity.class));
        UserResponseDto savedUserResponse = mapper.convertObject(savedUser, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(savedUserResponse);
        }
        return savedUserResponse;
    }

    @Override
    public CollectionModel<UserResponseDto> findAll(Boolean hateoasEnabled) {
        List<UserEntity> allUsers = userRepository.findAll();
        List<UserResponseDto> userResponses;
        if (!hateoasEnabled) {
            userResponses = mapper.convertList(allUsers, UserResponseDto.class);
            return CollectionModel.of(userResponses);
        }
        userResponses = allUsers.stream()
                .map(user -> mapper.convertObject(user, UserResponseDto.class))
                .map(this::applyLinks)
                .toList();
        return CollectionModel.of(
                userResponses,
                linkTo(methodOn(UserController.class).findAll(true)).withSelfRel()
        );
    }

    @Override
    public PagedModel<UserResponseDto> findByNameContainingIgnoreCase(String name, Boolean hateoasEnabled, Pageable pageable) {
        Page<UserEntity> paginatedUsers = userRepository.findByNameContainingIgnoreCase(name, pageable);
        Page<UserResponseDto> paginatedUserResponses;
        if (hateoasEnabled) {
            paginatedUserResponses = paginatedUsers
                    .map(userEntity -> mapper.convertObject(userEntity, UserResponseDto.class))
                    .map(this::applyLinks);
            return applyPageLinks(paginatedUserResponses);
        }
        paginatedUserResponses = paginatedUsers.map(userEntity -> mapper.convertObject(userEntity, UserResponseDto.class));
        return PagedModel.of(
                paginatedUserResponses.getContent(),
                new PagedModel.PageMetadata(
                        paginatedUserResponses.getSize(),
                        paginatedUserResponses.getNumber(),
                        paginatedUserResponses.getTotalElements(),
                        paginatedUserResponses.getTotalPages()
                )
        );
    }

    @Override
    @Transactional
    public UserResponseDto update(UUID id, UserRequestDto userUpdate, Boolean hateoasEnabled) {
        UserEntity foundUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        mapper.copyProperties(userUpdate, foundUser);
        UserEntity updatedUser = userRepository.save(foundUser);
        UserResponseDto updatedUserResponse = mapper.convertObject(updatedUser, UserResponseDto.class);
        if (hateoasEnabled) {
            applyLinks(updatedUserResponse);
        }
        return updatedUserResponse;
    }

    @Override
    public void delete(UUID id) {
        UserEntity foundUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " was not found!"));
        userRepository.delete(foundUser);
    }

    private UserResponseDto applyLinks(UserResponseDto userResponseDto) {
        try {
            userResponseDto.add(linkTo(methodOn(UserController.class).findById(userResponseDto.getId(), true)).withSelfRel());
            userResponseDto.add(linkTo(methodOn(UserController.class).save(new UserRequestDto("John Doe", 25), true)).withRel("create"));
            userResponseDto.add(linkTo(methodOn(UserController.class).delete(userResponseDto.getId())).withRel("delete"));
            userResponseDto.add(linkTo(methodOn(UserController.class).update(userResponseDto.getId(), new UserRequestDto("John Doe", 25), true)).withRel("update"));
            return userResponseDto;
        } catch (Exception exception) {
            throw new RuntimeException("Links could not be added to object " + userResponseDto.getClass().getName() + "!");
        }

    }

    private PagedModel<UserResponseDto> applyPageLinks(Page<UserResponseDto> page) {
        int size = page.getSize(), number = page.getNumber();
        long totalElements = page.getTotalElements();
        List<Link> links = new ArrayList<>();
        UriComponentsBuilder uriBuilder = ServletUriComponentsBuilder.fromCurrentRequest();

        // Método auxiliar para garantir a ordem dos parâmetros
        Function<Integer, String> buildOrderedUri = (pageNumber) -> {
            UriComponentsBuilder adjustedUriBuilder = uriBuilder.cloneBuilder()
                    .replaceQueryParam("name", uriBuilder.build().getQueryParams().getFirst("name")) // Preserva 'name'
                    .replaceQueryParam("size", size) // Define 'size'
                    .replaceQueryParam("page", pageNumber) // Define 'page'
                    .replaceQueryParam("sort", uriBuilder.build().getQueryParams().getFirst("sort")) // Preserva 'sort'
                    .replaceQueryParam("hateoas", "true"); // Garante 'hateoas=true'
            return adjustedUriBuilder.toUriString();
        };

        // self
        links.add(Link.of(buildOrderedUri.apply(number)).withSelfRel());

        // first
        links.add(Link.of(buildOrderedUri.apply(0)).withRel("first"));

        // last
        int lastPage = (int) Math.ceil((double) totalElements / size) - 1;
        lastPage = Math.max(lastPage, 0);
        links.add(Link.of(buildOrderedUri.apply(lastPage)).withRel("last"));

        // next
        if (number < lastPage) {
            links.add(Link.of(buildOrderedUri.apply(number + 1)).withRel("next"));
        }

        // prev
        if (number > 0) {
            links.add(Link.of(buildOrderedUri.apply(number - 1)).withRel("prev"));
        }

        return PagedModel.of(
                page.getContent(),
                new PagedModel.PageMetadata(size, number, totalElements, lastPage + 1),
                links
        );
    }
}
