package com.sky.identity.usermanagement.validator;

import com.sky.identity.usermanagement.domain.repository.UserRepository;
import com.sky.identity.usermanagement.exception.UserAlreadyExistsException;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUser(String username, String email) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new UserAlreadyExistsException("Username already exists");
        });

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new UserAlreadyExistsException("Email already exists");
        });
    }
}
