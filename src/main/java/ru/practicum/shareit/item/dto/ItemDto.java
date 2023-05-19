package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ItemDto {
    private long id;
    @NotBlank(message = "name field cannot be empty")
    private String name;
    @NotBlank(message = "description field cannot be empty")
    private String description;
    @BooleanFlag
    @NotNull(message = "available field cannot be empty")
    private Boolean available;
}
