package run.halo.app.model.params;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import run.halo.app.model.support.CreateCheck;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author johnniang
 * @date 19-6-1
 */
@Slf4j
public class InstallParamTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void createCheckTest() {
        InstallParam installParam = new InstallParam();

        Set<ConstraintViolation<InstallParam>> constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertThat(constraintViolations.size(), equalTo(4));
        printMessage(constraintViolations);

        installParam.setEmail("test");
        constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertThat(constraintViolations.size(), equalTo(4));
        printMessage(constraintViolations);

        installParam.setEmail("test@test.com");
        constraintViolations = validator.validate(installParam, CreateCheck.class);
        assertThat(constraintViolations.size(), equalTo(3));
        printMessage(constraintViolations);
    }

    private void printMessage(Set<ConstraintViolation<InstallParam>> constraintViolations) {
        if (constraintViolations == null) {
            return;
        }

        log.debug("");

        constraintViolations.forEach(constraintViolation -> log.debug(constraintViolation.getMessage()));
    }
}