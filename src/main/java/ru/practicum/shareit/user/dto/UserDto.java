package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.valid.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class UserDto {
    private long id;
    @NotBlank(
            groups = Marker.OnCreate.class,
            message = "name field cannot be empty"
    )
    private String name;
    @NotBlank(
            groups = Marker.OnCreate.class,
            message = "email field cannot be empty"
    )
    @Email(
            groups = {Marker.OnCreate.class, Marker.OnUpdate.class},
            message = "incorrectly entered email"
    )
    private String email;
}
