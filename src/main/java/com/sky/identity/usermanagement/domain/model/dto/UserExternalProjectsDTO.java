package com.sky.identity.usermanagement.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserExternalProjectsDTO {

    UserDTO userDTO;
    Long id;
    String projectName;
}
