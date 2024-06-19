package com.sky.identity.usermanagement.domain.repository;

import com.sky.identity.usermanagement.domain.model.entity.UserExternalProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserExternalProjectsRepository extends JpaRepository<UserExternalProject, String> {

    Optional<UserExternalProject> findByNameAndUserId(String projectName, Long userId);

    List<UserExternalProject> findAllByUserId(Long userId);
}
