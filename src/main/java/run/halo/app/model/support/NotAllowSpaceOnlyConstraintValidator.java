package run.halo.app.model.support;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * Not allow space only validator but allow "".
 *
 * @author guqing
 * @date 2022-02-28
 */
public class NotAllowSpaceOnlyConstraintValidator implements
    ConstraintValidator<NotAllowSpaceOnly, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (Objects.equals(value, "")) {
            return true;
        }
        return StringUtils.equals(StringUtils.trim(value), value);
    }
}
