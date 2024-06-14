package com.sky.identity.usermanagement.validator;

import com.sky.identity.usermanagement.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void validateUser(String username, String email) {
        userRepository.findByUsername(username).ifPresent(existingUser -> {
            throw new IllegalArgumentException("Username already exists");
        });

        userRepository.findByEmail(email).ifPresent(existingUser -> {
            throw new IllegalArgumentException("Email already exists");
        });
    }
}
