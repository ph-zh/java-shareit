package ru.practicum.shareit.booking.valid;

import ru.practicum.shareit.booking.dto.BookingCreationDto;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class CheckDateValidator implements ConstraintValidator<StartBeforeEndDateValid, BookingCreationDto> {

    @Override
    public void initialize(StartBeforeEndDateValid constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(BookingCreationDto bookingCreationDto, ConstraintValidatorContext constraintValidatorContext) {
        LocalDateTime start = bookingCreationDto.getStart();
        LocalDateTime end = bookingCreationDto.getEnd();
        if (start == null || end == null) {
            return false;
        }
        return start.isBefore(end);
    }
}
