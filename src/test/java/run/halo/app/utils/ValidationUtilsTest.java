package run.halo.app.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Validation utils test.
 *
 * @author johnniang
 */
class ValidationUtilsTest {

    Validator validator = ValidationUtils.getValidator();

    @Test
    void validateObjectTest() {
        Car car = new Car(null);
        Set<ConstraintViolation<Car>> violations = validator.validate(car);
        validateObjectAssert(violations);

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
            () -> ValidationUtils.validate(car));
        validateObjectAssert(exception.getConstraintViolations());
    }

    void validateObjectAssert(Set<? extends ConstraintViolation<?>> violations) {
        assertEquals(1, violations.size());
        ConstraintViolation<?> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Name must not be blank", violation.getMessage());
    }

    @Test
    void validateListTest() {
        List<Car> cars = Arrays.asList(new Car(""),
            new Car("car name"),
            new Car(null));

        ConstraintViolationException exception = assertThrows(ConstraintViolationException.class,
            () -> ValidationUtils.validate(cars));

        validateIteratorTest(exception.getConstraintViolations());
    }

    void validateIteratorTest(Set<? extends ConstraintViolation<?>> violations) {
        assertEquals(2, violations.size());

        LinkedList<? extends ConstraintViolation<?>> violationList = new LinkedList<>(violations);
        violationList.sort(Comparator.comparing(v -> v.getPropertyPath().toString()));

        // get first violation
        ConstraintViolation<?> firstViolation = violationList.get(0);
        // get second violation
        ConstraintViolation<?> secondViolation = violationList.get(1);

        assertEquals("name[0]", firstViolation.getPropertyPath().toString());
        assertEquals("name[2]", secondViolation.getPropertyPath().toString());
    }

    /**
     * Car entity.
     *
     * @author johnniang
     */
    @Data
    @AllArgsConstructor
    static class Car {

        @NotBlank(message = "Name must not be blank")
        private String name;

    }

}