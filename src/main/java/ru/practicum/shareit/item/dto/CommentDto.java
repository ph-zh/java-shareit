package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Positive;
import java.time.Instant;

@Builder
@Data
public class CommentDto {
    @Positive
    private Long id;
    @NotBlank
    private String text;
    @NotBlank
    private String authorName;
    @Past
    private Instant instant;
}
