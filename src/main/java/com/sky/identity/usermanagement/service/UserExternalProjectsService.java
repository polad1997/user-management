package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.dto.ExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.entity.User;
import com.sky.identity.usermanagement.domain.model.entity.UserExternalProject;
import com.sky.identity.usermanagement.domain.repository.UserExternalProjectsRepository;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.ProjectAlreadyAssignedException;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserExternalProjectsService {

    private final UserRepository userRepository;
    private final UserExternalProjectsRepository userExternalProjectsRepository;

    public UserExternalProjectsService(UserRepository userRepository, UserExternalProjectsRepository userExternalProjectsRepository) {
        this.userRepository = userRepository;
        this.userExternalProjectsRepository = userExternalProjectsRepository;
    }

    public UserExternalProjectsDTO addExternalProjectToUser(Long userId, String projectName) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        checkIfProjectAlreadyAssigned(user, projectName);
        var project = createNewProject(user, projectName);
        var savedProject = userExternalProjectsRepository.save(project);

        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getUsername());

        return new UserExternalProjectsDTO(userDTO, savedProject.getId(), savedProject.getName());
    }

    public List<ExternalProjectsDTO> getExternalProjectsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        var externalProjects = userExternalProjectsRepository.findAllByUserId(userId);
        return mapToUserExternalProjectsDTOs(externalProjects);
    }

    private void checkIfProjectAlreadyAssigned(User user, String projectName) {
        Optional<UserExternalProject> existingProject = userExternalProjectsRepository.findByNameAndUserId(projectName, user.getId());
        if (existingProject.isPresent()) {
            throw new ProjectAlreadyAssignedException("Project is already assigned to the user id: " + user.getId());
        }
    }

    private UserExternalProject createNewProject(User user, String projectName) {
        var newProject = new UserExternalProject();
        newProject.setUser(user);
        newProject.setName(projectName);
        return newProject;
    }

    private List<ExternalProjectsDTO> mapToUserExternalProjectsDTOs(List<UserExternalProject> externalProjects) {
        return externalProjects.stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    private ExternalProjectsDTO mapEntityToDto(UserExternalProject entity) {
        var dto = new ExternalProjectsDTO();
        dto.setId(entity.getId());
        dto.setProjectName(entity.getName());
        return dto;
    }

}
