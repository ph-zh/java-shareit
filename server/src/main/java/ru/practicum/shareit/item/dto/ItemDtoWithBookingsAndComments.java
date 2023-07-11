package ru.practicum.shareit.item.dto;

import jdk.jfr.BooleanFlag;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoForItem;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
public class ItemDtoWithBookingsAndComments {
    private Long id;
    @NotBlank(message = "name field cannot be empty")
    private String name;
    @NotBlank(message = "description field cannot be empty")
    private String description;
    @BooleanFlag
    @NotNull(message = "available field cannot be empty")
    private Boolean available;
    private BookingDtoForItem lastBooking;
    private BookingDtoForItem nextBooking;
    private List<CommentDto> comments;
}
