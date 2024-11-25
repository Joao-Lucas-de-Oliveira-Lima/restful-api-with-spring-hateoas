package edu.jl.springhateoas.controller;

import edu.jl.springhateoas.dto.user.UserRequestDto;
import edu.jl.springhateoas.dto.user.UserResponseDto;
import edu.jl.springhateoas.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users/v1")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> findById(
            @PathVariable(name = "id") UUID id,
            @RequestParam(name = "hateoas", defaultValue = "false") Boolean hateoasEnabled) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findById(id, hateoasEnabled));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<UserResponseDto>> findAll(
            @RequestParam(name = "hateoas", defaultValue = "false") Boolean hateoasEnabled) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.findAll(hateoasEnabled));
    }

    @GetMapping("/paged")
    @PageableAsQueryParam
    public ResponseEntity<PagedModel<UserResponseDto>> findByNameContainingIgnoreCase(
            @RequestParam(name = "name", defaultValue = "") String name,
            @RequestParam(name = "hateoas", defaultValue = "false") Boolean hateoasEnabled,
            @Parameter(hidden = true)
            @PageableDefault(
                    size = 20,
                    page = 0,
                    direction = Sort.Direction.ASC,
                    sort = {"name"})
            Pageable pageable) {

        return ResponseEntity.ok(userService.findByNameContainingIgnoreCase(name, hateoasEnabled, pageable));
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> save(
            @Valid @RequestBody UserRequestDto userRequestDto,
            @RequestParam(name = "hateoas", defaultValue = "false") Boolean hateoasEnabled,
            BindingResult requestValidationErrors) throws BadRequestException{
        if(requestValidationErrors.hasErrors()){
            throw new BadRequestException("Data not provided or incorrect!");
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.save(userRequestDto, hateoasEnabled));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable(name = "id") UUID id,
            @Valid @RequestBody UserRequestDto userUpdate,
            @RequestParam(name = "hateoas", defaultValue = "false") Boolean hateoasEnabled,
            BindingResult requestValidationErrors) throws BadRequestException{
        if(requestValidationErrors.hasErrors()){
            throw new BadRequestException("Data not provided or incorrect!");
        }
        return ResponseEntity.ok(userService.update(id, userUpdate, hateoasEnabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") UUID id) {
        userService.delete(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

}
