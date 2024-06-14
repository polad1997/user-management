package com.sky.identity.usermanagement.domain.repository;

import com.sky.identity.usermanagement.domain.model.entity.UserExternalProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExternalProjectRepository extends JpaRepository<UserExternalProject, String> {
}
