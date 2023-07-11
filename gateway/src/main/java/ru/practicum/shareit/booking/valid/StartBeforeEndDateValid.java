package ru.practicum.shareit.booking.valid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckDateValidator.class)
public @interface StartBeforeEndDateValid {
    String message() default "the end of the booking cannot be earlier than the start of the booking";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
