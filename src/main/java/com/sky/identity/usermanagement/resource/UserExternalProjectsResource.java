package com.sky.identity.usermanagement.resource;

import com.sky.identity.usermanagement.domain.model.dto.UserExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.request.AddExternalProjectToUserRequest;
import com.sky.identity.usermanagement.service.UserExternalProjectsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/{id}/external-projects")
public class UserExternalProjectsResource {

    private final UserExternalProjectsService userExternalProjectsService;

    public UserExternalProjectsResource(UserExternalProjectsService userExternalProjectsService) {
        this.userExternalProjectsService = userExternalProjectsService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserExternalProjectsDTO> addExternalProjectToUser(@PathVariable Long id, @RequestBody @Valid AddExternalProjectToUserRequest request) {
        var response = userExternalProjectsService.addExternalProjectToUser(id, request.getExternalProjectName());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
