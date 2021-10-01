package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

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

    @Test
    void emailValidateTest() {
        // Valid email addresses
        assertTrue(ValidationUtils.isEmail("example@example.com"));
        assertTrue(ValidationUtils.isEmail("12313213@example.com"));
        assertTrue(ValidationUtils.isEmail("user.name@example.com.cn"));
        assertTrue(ValidationUtils.isEmail("very.common@example.com"));
        assertTrue(ValidationUtils.isEmail("disposable.style.email.with+symbol@example.com"));
        assertTrue(ValidationUtils.isEmail("other.email-with-hyphen@example.com"));
        assertTrue(ValidationUtils.isEmail("fully-qualified-domain@example.com"));
        assertTrue(ValidationUtils.isEmail("user.name+tag+sorting@example.com"));
        assertTrue(ValidationUtils.isEmail("x@example.com"));
        assertTrue(ValidationUtils.isEmail("example-indeed@strange-example.com"));
        assertTrue(ValidationUtils.isEmail("example@s.example"));
        assertTrue(ValidationUtils.isEmail("\"john..doe\"@example.org"));

        // Invalid email addresses
        assertFalse(ValidationUtils.isEmail("Abc.example.com"));
        assertFalse(ValidationUtils.isEmail("A@b@c@example.com"));
        assertFalse(ValidationUtils.isEmail("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com"));
        assertFalse(ValidationUtils.isEmail("just\"not\"right@example.com"));
        assertFalse(ValidationUtils.isEmail("this is\"not\\allowed@example.com"));
        assertFalse(ValidationUtils.isEmail("this\\ still\\\"not\\\\allowed@example.com"));
        assertFalse(ValidationUtils.isEmail("john..doe@example.com"));
        assertFalse(ValidationUtils.isEmail("john.doe@example..com"));
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