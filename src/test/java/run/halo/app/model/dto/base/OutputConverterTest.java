package run.halo.app.model.dto.base;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.junit.jupiter.api.Test;

/**
 * Output converter test.
 *
 * @author johnniang
 */
class OutputConverterTest {

    @Test
    void convertFromTest() {
        TestDomain domain = new TestDomain("test_domain_name", 10);

        TestOutputDTO testOutputDTO = new TestOutputDTO().convertFrom(domain);

        assertEquals("test_domain_name", testOutputDTO.getName());
    }

    @Test
    void convertFromSubTest() {
        TestDomain domain = new TestDomain("test_domain_name", 10);

        SubTestOutputDTO subTestOutputDTO = new SubTestOutputDTO().convertFrom(domain);

        assertEquals("test_domain_name", subTestOutputDTO.getName());
        assertEquals(10, subTestOutputDTO.getAge());
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestDomain {

        private String name;

        private Integer age;
    }

    @Data
    @ToString
    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    static class TestOutputDTO implements OutputConverter<TestOutputDTO, TestDomain> {

        private String name;
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    static class SubTestOutputDTO extends TestOutputDTO {
        private Integer age;
    }
}
