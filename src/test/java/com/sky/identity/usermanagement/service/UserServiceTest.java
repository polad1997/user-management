package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.entity.Role;
import com.sky.identity.usermanagement.domain.model.entity.User;
import com.sky.identity.usermanagement.domain.model.request.CreateUserRequest;
import com.sky.identity.usermanagement.domain.model.request.UpdatePasswordRequest;
import com.sky.identity.usermanagement.domain.repository.RoleRepository;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.UserAlreadyExistsException;
import com.sky.identity.usermanagement.exception.UserNotFoundException;
import com.sky.identity.usermanagement.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUser_Success() {
        CreateUserRequest request = new CreateUserRequest("username", "email@example.com", "password");
        User user = new User();
        user.setId(2L);
        user.setEmail("email@example.com");
        user.setUsername("username");

        Role role = new Role();
        role.setId(2L);
        role.setName("USER");

        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName(any())).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleService.getdefaultUserRole()).thenReturn(role);

        UserDTO userDTO = userService.createUser(request);

        assertNotNull(userDTO);
        assertEquals("email@example.com", userDTO.getEmail());
        assertEquals("username", userDTO.getUsername());
    }

//    @Test  fixme
//    public void testCreateUser_EmailAlreadyExists() {
//        CreateUserRequest request = new CreateUserRequest("username", "email@example.com", "password");
//
//        when(userRepository.findByUsername("username")).thenReturn(Optional.empty());
//        when(userRepository.findByEmail("email@example.com")).thenReturn(Optional.of(new User()));
//
//        Exception exception = assertThrows(UserAlreadyExistsException.class, () -> {
//            userService.createUser(request);
//        });
//
//        assertEquals("Email already exists", exception.getMessage());
//    }

    @Test
    public void testGetUserById_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("email@example.com");
        user.setUsername("username");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO userDTO = userService.getUserById(1L);

        assertNotNull(userDTO);
        assertEquals("email@example.com", userDTO.getEmail());
        assertEquals("username", userDTO.getUsername());
    }

    @Test
    public void testGetUserById_UserNotFound() {
        when(userRepository.findById(11L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(11L);
        });

        assertEquals("User not found with id: 11", exception.getMessage());
    }

    @Test
    public void testDeleteUserById_Success() {
        doNothing().when(userRepository).deleteById(1L);

        assertDoesNotThrow(() -> {
            userService.deleteUserById(1L);
        });

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUserById_UserNotFound() {
        doThrow(new UserNotFoundException("User not found with id: 1")).when(userRepository).deleteById(1L);

        Exception exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUserById(1L);
        });

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    public void testUpdateUserPassword_Success() {
        UpdatePasswordRequest request = new UpdatePasswordRequest(1L, "newPassword");
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setEmail("example@example.com");
        user.setPassword("oldPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        UserDTO userDTO = userService.updateUserPassword(request);

        assertNotNull(userDTO);
        assertEquals("encodedNewPassword", user.getPassword());
        assertEquals(1L, userDTO.getId());
        verify(userRepository, times(1)).save(user);
    }


    @Test
    public void testUpdateUserPassword_InvalidPassword() {
        UpdatePasswordRequest request = new UpdatePasswordRequest(1L, "");

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserPassword(request);
        });

        assertEquals("Password must not be empty", exception.getMessage());
    }
}
