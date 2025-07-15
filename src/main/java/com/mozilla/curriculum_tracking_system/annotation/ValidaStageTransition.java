package com.mozilla.curriculum_tracking_system.annotation;

import com.mozilla.curriculum_tracking_system.util.tracking.ValidStageTransitionValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ValidStageTransitionValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidaStageTransition {
    String message() default "Invalid stage transition";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
