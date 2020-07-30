package run.halo.app.model.params;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import run.halo.app.model.support.CreateCheck;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author johnniang
 * @date 19-6-1
 */
@Slf4j
class InstallParamTest {

    final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void createCheckTest() {
        InstallParam installParam = new InstallParam();

        Set<ConstraintViolation<InstallParam>> constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertEquals(4, constraintViolations.size());

        printMessage(constraintViolations);

        installParam.setEmail("test");
        constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertEquals(4, constraintViolations.size());

        printMessage(constraintViolations);

        installParam.setEmail("test@test.com");
        constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertEquals(3, constraintViolations.size());
        printMessage(constraintViolations);
    }

    void printMessage(Set<ConstraintViolation<InstallParam>> constraintViolations) {
        if (constraintViolations == null) {
            return;
        }

        log.debug("");

        constraintViolations.forEach(constraintViolation -> log.debug(constraintViolation.getMessage()));
    }
}