package com.gamecontrol.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gamecontrol.enums.Role;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private String id;
    private String email;
    private String username;
    private String bio;
    private String profilePictureUrl;
    private LocalDate birthDate;
    private String country;
    private Role role;
}
