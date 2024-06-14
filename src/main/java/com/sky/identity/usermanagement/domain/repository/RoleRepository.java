package com.sky.identity.usermanagement.domain.repository;

import com.sky.identity.usermanagement.domain.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
