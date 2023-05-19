package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class User {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
}
