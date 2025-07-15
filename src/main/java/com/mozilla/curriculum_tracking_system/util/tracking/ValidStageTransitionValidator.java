package com.mozilla.curriculum_tracking_system.util.tracking;

import com.mozilla.curriculum_tracking_system.annotation.ValidaStageTransition;
import com.mozilla.curriculum_tracking_system.dto.tracking.TrackingActionRequest;
import com.mozilla.curriculum_tracking_system.enums.TrackingAction;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

/**
 * Validator for tracking stage transitions
 */
@RequiredArgsConstructor
public class ValidStageTransitionValidator implements ConstraintValidator<ValidaStageTransition, TrackingActionRequest> {
    @Override
    public void initialize(ValidaStageTransition constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TrackingActionRequest trackingActionRequest, ConstraintValidatorContext constraintValidatorContext) {
        if (trackingActionRequest == null) {
            return true; // Let @NotNull handle null validation
        }

        if (trackingActionRequest.getAction() == TrackingAction.RETURN && trackingActionRequest.getReturnToStage() == null) {
            constraintValidatorContext.disableDefaultConstraintViolation();
            constraintValidatorContext.buildConstraintViolationWithTemplate("Return stage must be specified for RETURN action")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
