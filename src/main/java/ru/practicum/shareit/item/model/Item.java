package ru.practicum.shareit.item.model;

import jdk.jfr.BooleanFlag;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class Item {
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @BooleanFlag
    @NotNull
    private Boolean available;
    private long owner;
}
