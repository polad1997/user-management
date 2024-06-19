package com.sky.identity.usermanagement.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.identity.usermanagement.domain.model.dto.ExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.dto.UserExternalProjectsDTO;
import com.sky.identity.usermanagement.domain.model.request.AddExternalProjectToUserRequest;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import com.sky.identity.usermanagement.service.UserExternalProjectsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class UserExternalProjectsResourceTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserExternalProjectsService userExternalProjectsService;

    @InjectMocks
    private UserExternalProjectsResource userExternalProjectsResource;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addExternalProjectToUser_Success() throws Exception {
        Long userId = 1L;
        String projectName = "Test Project";

        UserDTO userDTO = new UserDTO(userId, "test@example.com", "username");
        UserExternalProjectsDTO userExternalProjectsDTO = new UserExternalProjectsDTO(userDTO, userId, projectName);
        AddExternalProjectToUserRequest request = new AddExternalProjectToUserRequest();
        request.setExternalProjectName("Test Project");

        when(userExternalProjectsService.addExternalProjectToUser(eq(userId), eq(projectName)))
                .thenReturn(userExternalProjectsDTO);

        mockMvc.perform(post("/users/{id}/external-projects", userId)
                        .param("id", userId.toString())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userExternalProjectsDTO)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_EmailIsMandatory() throws Exception {
        AddExternalProjectToUserRequest request = new AddExternalProjectToUserRequest();
        request.setExternalProjectName("");
        Long userId = 1L;

        mockMvc.perform(post("/users/{id}/external-projects", userId)
                        .param("id", userId.toString())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.externalProjectName").value("External project name is mandatory"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addExternalProjectToUser_Forbidden() throws Exception {
        AddExternalProjectToUserRequest request = new AddExternalProjectToUserRequest();
        request.setExternalProjectName("test project");
        Long userId = 1L;

        mockMvc.perform(post("/users/{id}/external-projects", userId)
                        .param("id", userId.toString())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void addExternalProjectToUser_UserNotFound() throws Exception {
        Long userId = 999L;
        AddExternalProjectToUserRequest request = new AddExternalProjectToUserRequest();
        request.setExternalProjectName("test project");

        doThrow(new UserNotFoundException("User not found with id: " + userId)).when(userExternalProjectsService).addExternalProjectToUser(userId, request.externalProjectName);

        mockMvc.perform(post("/users/{id}/external-projects", userId)
                        .param("id", userId.toString())
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + userId));

        verify(userExternalProjectsService, times(1)).addExternalProjectToUser(userId, request.getExternalProjectName());

    }

    @Test
    @WithMockUser(roles = "USER")
    void getExternalProjectsByUser_Success() throws Exception {
        Long userId = 1L;

        ExternalProjectsDTO project1 = new ExternalProjectsDTO(1L, "Project 1");
        ExternalProjectsDTO project2 = new ExternalProjectsDTO(2L, "Project 2");

        List<ExternalProjectsDTO> externalProjectsDTO = new ArrayList<>();
        externalProjectsDTO.add(project1);
        externalProjectsDTO.add(project2);

        when(userExternalProjectsService.getExternalProjectsByUser(eq(userId)))
                .thenReturn(externalProjectsDTO);

        mockMvc.perform(get("/users/{id}/external-projects", userId)
                        .param("id", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(externalProjectsDTO)));

        verify(userExternalProjectsService, times(1)).getExternalProjectsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getExternalProjectsByUser_UserNotFound() throws Exception {
        Long userId = 999L;

        when(userExternalProjectsService.getExternalProjectsByUser(eq(userId)))
                .thenThrow(new UserNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/users/{id}/external-projects", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"User not found with id: " + userId + "\"}"));

        verify(userExternalProjectsService, times(1)).getExternalProjectsByUser(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getExternalProjectsByUser_NoProjects() throws Exception {
        Long userId = 1L;

        List<ExternalProjectsDTO> externalProjectsDTO = new ArrayList<>();

        when(userExternalProjectsService.getExternalProjectsByUser(eq(userId)))
                .thenReturn(externalProjectsDTO);

        mockMvc.perform(get("/users/{id}/external-projects", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(externalProjectsDTO)));

        verify(userExternalProjectsService, times(1)).getExternalProjectsByUser(userId);
    }

}
