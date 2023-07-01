package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.user.valid.Marker;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
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
