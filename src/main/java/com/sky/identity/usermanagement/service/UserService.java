package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.dto.UserDTO;
import com.sky.identity.usermanagement.domain.model.entity.User;
import com.sky.identity.usermanagement.domain.model.request.CreateUserRequest;
import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.validator.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(UserRepository userRepository, UserValidator userValidator, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public UserDTO createUser(CreateUserRequest request) {

        userValidator.validateUser(request.getUsername(), request.getEmail());
        var user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getUsername());
        user.getRoles().add(roleService.getdefaultUserRole());

        var savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getUsername());
    }
}
