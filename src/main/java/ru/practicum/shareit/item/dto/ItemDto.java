package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class ItemDto {
    private Long id;
    @NotBlank(message = "name field cannot be empty")
    private String name;
    @NotBlank(message = "description field cannot be empty")
    private String description;
    private Long owner;
    @BooleanFlag
    @NotNull(message = "available field cannot be empty")
    private Boolean available;
    @Positive
    private Long requestId;
}
