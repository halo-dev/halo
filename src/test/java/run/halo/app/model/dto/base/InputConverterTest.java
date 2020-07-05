package run.halo.app.model.dto.base;

import lombok.*;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InputConverterTest {

    @Test
    void convertToTest() {
        TestInputDTO inputDTO = new TestInputDTO("test_name");

        TestDomain domain = inputDTO.convertTo();

        assertEquals("test_name", domain.getName());
        assertNull(domain.getAge());
    }

    @Test
    void updateTest() {
        TestInputDTO inputDTO = new TestInputDTO("test_input_dto_name");

        TestDomain domain = new TestDomain("test_domain_name", 10);

        inputDTO.update(domain);

        assertEquals("test_input_dto_name", domain.getName());
        assertEquals(10, domain.getAge());
    }

    @Test
    void subConvertToTest() {
        SubTestInputDTO subTestInputDTO = new SubTestInputDTO();
        subTestInputDTO.setName("test_name");
        subTestInputDTO.setAge(10);

        TestDomain domain = subTestInputDTO.convertTo();

        assertEquals("test_name", domain.getName());
        assertEquals(10, domain.getAge());
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestDomain {

        private String name;

        private Integer age;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestInputDTO implements InputConverter<TestDomain>, Serializable {

        private String name;
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    static class SubTestInputDTO extends TestInputDTO {

        private Integer age;
    }
}
