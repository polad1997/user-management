package com.sky.identity.usermanagement.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddExternalProjectToUserRequest {

    @NotNull(message = "External project name is mandatory")
    @NotBlank(message = "External project name is mandatory")
    public String externalProjectName;
}
