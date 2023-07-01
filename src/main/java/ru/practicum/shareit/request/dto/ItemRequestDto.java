package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ItemRequestDto {
    @Positive
    @NotNull
    private Long id;
    @NotBlank
    private String description;
    @Positive
    @NotNull
    private Long requestor;
    @Past
    private Instant created;
    private List<ItemDto> items;
}
