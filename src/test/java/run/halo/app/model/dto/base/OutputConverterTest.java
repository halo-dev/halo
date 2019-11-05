package run.halo.app.model.dto.base;

import lombok.*;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Output converter test.
 *
 * @author johnniang
 */
public class OutputConverterTest {

    @Test
    public void convertFromTest() {
        TestDomain domain = new TestDomain("test_domain_name", 10);

        TestOutputDTO testOutputDTO = new TestOutputDTO().convertFrom(domain);

        assertThat(testOutputDTO.getName(), equalTo("test_domain_name"));
    }

    @Test
    public void convertFromSubTest() {
        TestDomain domain = new TestDomain("test_domain_name", 10);

        SubTestOutputDTO subTestOutputDTO = new SubTestOutputDTO().convertFrom(domain);

        assertThat(subTestOutputDTO.getName(), equalTo("test_domain_name"));
        assertThat(subTestOutputDTO.getAge(), equalTo(10));
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
    public static class TestOutputDTO implements OutputConverter<TestOutputDTO, TestDomain> {

        private String name;
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTestOutputDTO extends TestOutputDTO {
        private Integer age;
    }
}
