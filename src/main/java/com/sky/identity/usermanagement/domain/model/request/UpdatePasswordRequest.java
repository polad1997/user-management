package com.sky.identity.usermanagement.domain.model.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePasswordRequest {

    public Long id;

    @NotNull(message = "Password is mandatory")
    @NotEmpty(message = "Password is mandatory")
    public String password;
}
