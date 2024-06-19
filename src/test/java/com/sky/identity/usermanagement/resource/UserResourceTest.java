package com.sky.identity.usermanagement.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.request.CreateUserRequest;
import com.sky.identity.usermanagement.domain.model.request.UpdatePasswordRequest;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.UserAlreadyExistsException;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import com.sky.identity.usermanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/clear-database.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class UserResourceTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @InjectMocks
    private UserResource userResource;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setEmail("testuser@example.com");
        createUserRequest.setPassword("password");

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("testuser");
        userDTO.setEmail("testuser@example.com");

        when(userService.createUser(createUserRequest)).thenReturn(userDTO);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(result -> assertEquals(HttpStatus.CREATED.value(), result.getResponse().getStatus()))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).createUser(createUserRequest);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_EmailIsMandatory() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setPassword("password");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email is mandatory"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_Forbidden() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("testuser");
        createUserRequest.setEmail("testuser@example.com");
        createUserRequest.setPassword("password");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_UserAlreadyExists() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("poladtestuser");
        createUserRequest.setPassword("password");
        createUserRequest.setEmail("poladtestuser@example.com");

        when(userService.createUser(createUserRequest)).thenThrow(new UserAlreadyExistsException("Username already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Username already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_Success_User_Forbidden() throws Exception {
        Long userId = 1L;

        doNothing().when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_NotFound() throws Exception {
        Long userId = 999L;

        doThrow(new UserNotFoundException("User not found with id: " + userId)).when(userService).deleteUserById(userId);

        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + userId));

        verify(userService, times(1)).deleteUserById(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserPassword_Success() throws Exception {
        Long userId = 1L;
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setId(userId);
        request.setPassword("newpassword");

        UserDTO userDTO = new UserDTO(userId, "testuser@example.com", "testuser");

        when(userService.updateUserPassword(request)).thenReturn(userDTO);

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService, times(1)).updateUserPassword(request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserPassword_UserNotFound() throws Exception {
        Long userId = 999L;
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setId(userId);
        request.setPassword("newpassword");

        when(userService.updateUserPassword(request)).thenThrow(new UserNotFoundException("User not found with id: " + userId));

        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + userId));

        verify(userService, times(1)).updateUserPassword(request);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUser_NotFound() throws Exception {
        Long userId = 23456L;

        when(userService.getUserById(userId)).thenThrow(new UserNotFoundException("User not found with id: " + userId));

        mockMvc.perform(get("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: " + userId));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateUser_PasswordIsMandatory() throws Exception {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setId(1L);
        request.setPassword("");

        when(userService.updateUserPassword(request)).thenReturn(new UserDTO());

        mockMvc.perform(put("/users/{id}", request.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").value("Password is mandatory"));
    }

}