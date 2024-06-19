package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.dto.ExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.entity.User;
import com.sky.identity.usermanagement.domain.model.entity.UserExternalProject;
import com.sky.identity.usermanagement.domain.repository.UserExternalProjectsRepository;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.ProjectAlreadyAssignedException;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserExternalProjectsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserExternalProjectsRepository userExternalProjectsRepository;

    @InjectMocks
    private UserExternalProjectsService userExternalProjectsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addExternalProjectToUser_UserNotFound() {
        Long userId = 1L;
        String projectName = "Test Project";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userExternalProjectsService.addExternalProjectToUser(userId, projectName));

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userExternalProjectsRepository, never()).save(any(UserExternalProject.class));
    }

    @Test
    void addExternalProjectToUser_ProjectAlreadyAssigned() {
        Long userId = 1L;
        String projectName = "Test Project";
        User user = new User(userId, "test@example.com", "password", "username", Set.of(), Set.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userExternalProjectsRepository.findByNameAndUserId(projectName, userId))
                .thenReturn(Optional.of(new UserExternalProject(1L, projectName, user)));

        ProjectAlreadyAssignedException exception = assertThrows(ProjectAlreadyAssignedException.class, () ->
                userExternalProjectsService.addExternalProjectToUser(userId, projectName));

        assertEquals("Project is already assigned to the user id: 1", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userExternalProjectsRepository, times(1)).findByNameAndUserId(projectName, userId);
        verify(userExternalProjectsRepository, never()).save(any(UserExternalProject.class));
    }

    @Test
    void addExternalProjectToUser_Success() {
        Long userId = 1L;
        String projectName = "Test Project";
        User user = new User(userId, "test@example.com", "password", "username", Set.of(), Set.of());
        UserExternalProject project = new UserExternalProject(1L, projectName, user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userExternalProjectsRepository.findByNameAndUserId(projectName, userId)).thenReturn(Optional.empty());
        when(userExternalProjectsRepository.save(any(UserExternalProject.class))).thenReturn(project);

        UserExternalProjectsDTO result = userExternalProjectsService.addExternalProjectToUser(userId, projectName);

        assertNotNull(result);
        assertEquals(project.getId(), result.getId());
        assertEquals(project.getName(), result.getProjectName());
        assertEquals(user.getId(), result.getUserDTO().getId());
        assertEquals(user.getEmail(), result.getUserDTO().getEmail());
        assertEquals(user.getUsername(), result.getUserDTO().getUsername());

        verify(userRepository, times(1)).findById(userId);
        verify(userExternalProjectsRepository, times(1)).findByNameAndUserId(projectName, userId);
        verify(userExternalProjectsRepository, times(1)).save(any(UserExternalProject.class));
    }

    @Test
    void getExternalProjectsByUser_UserHasProjects() {
        Long userId = 1L;
        User user = new User(userId, "test@example.com", "password", "username", Set.of(), Set.of());

        UserExternalProject project1 = new UserExternalProject(1L, "Project 1", null);
        UserExternalProject project2 = new UserExternalProject(2L, "Project 2", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userExternalProjectsRepository.findAllByUserId(userId))
                .thenReturn(List.of(project1, project2));

        List<ExternalProjectsDTO> result = userExternalProjectsService.getExternalProjectsByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Project 1", result.get(0).getProjectName());
        assertEquals("Project 2", result.get(1).getProjectName());

        verify(userExternalProjectsRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void getExternalProjectsByUser_UserHasNoProjects() {
        Long userId = 1L;
        User user = new User(userId, "test@example.com", "password", "username", Set.of(), Set.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userExternalProjectsRepository.findAllByUserId(userId))
                .thenReturn(List.of());

        List<ExternalProjectsDTO> result = userExternalProjectsService.getExternalProjectsByUser(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userExternalProjectsRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    void getExternalProjectsByUser_UserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
                userExternalProjectsService.getExternalProjectsByUser(userId));

        assertEquals("User not found with id: " + userId, exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userExternalProjectsRepository, never()).findAllByUserId(userId);
    }
}
