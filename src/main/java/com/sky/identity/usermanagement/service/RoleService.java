package com.sky.identity.usermanagement.service;

import com.sky.identity.usermanagement.domain.model.entity.Role;
import com.sky.identity.usermanagement.domain.repository.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getUserRole() {
        return roleRepository.findByName("ROLE_USER").orElseThrow(() -> new IllegalStateException("Role not found"));
    }
}
