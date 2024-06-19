package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.entity.User;
import com.sky.identity.usermanagement.domain.model.entity.UserExternalProject;
import com.sky.identity.usermanagement.domain.repository.UserExternalProjectsRepository;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

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
        var project = findOrCreateProject(user, projectName);
        userExternalProjectsRepository.save(project);

        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getUsername());

        return new UserExternalProjectsDTO(userDTO, project.getId(), project.getName());
    }

    private void checkIfProjectAlreadyAssigned(User user, String projectName) {
        boolean projectExists = user.getExternalProjects()
                .stream()
                .anyMatch(project -> project.getName().equals(projectName));
        if (projectExists) {
            throw new RuntimeException("Project is already assigned to the user: " + user.getUsername());
        }
    }

    private UserExternalProject findOrCreateProject(User user, String projectName) {
        return userExternalProjectsRepository.findByName(projectName)
                .map(existingProject -> updateExistingProject(user, existingProject))
                .orElseGet(() -> createNewProject(user, projectName));
    }

    private UserExternalProject updateExistingProject(User user, UserExternalProject existingProject) {
        existingProject.setUser(user);
        return existingProject;
    }

    private UserExternalProject createNewProject(User user, String projectName) {
        var newProject = new UserExternalProject();
        newProject.setUser(user);
        newProject.setName(projectName);
        return newProject;
    }
}
