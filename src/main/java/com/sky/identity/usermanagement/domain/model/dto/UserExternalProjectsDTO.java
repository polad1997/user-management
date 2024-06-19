package com.sky.identity.usermanagement.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExternalProjectsDTO {

    UserDTO user;
    Long id;
    String projectName;
}
