package com.sky.identity.usermanagement.domain.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExternalProjectsDTO {
    Long id;
    String projectName;
}
