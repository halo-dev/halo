package run.halo.app.model.support;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Not allow space only validate annotation.
 *
 * @author guqing
 * @date 2022-02-28
 */
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotAllowSpaceOnlyConstraintValidator.class)
@Target(value = {ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
public @interface NotAllowSpaceOnly {
    String message() default "开头和结尾不允许包含空格";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
