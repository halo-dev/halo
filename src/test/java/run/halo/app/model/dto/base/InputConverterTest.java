package run.halo.app.model.dto.base;

import lombok.*;
import org.junit.Test;

import java.io.Serializable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class InputConverterTest {

    @Test
    public void convertToTest() {
        TestInputDTO inputDTO = new TestInputDTO("test_name");

        TestDomain domain = inputDTO.convertTo();
        assertThat(domain.getName(), equalTo("test_name"));
        assertNull(domain.getAge());
    }

    @Test
    public void updateTest() {
        TestInputDTO inputDTO = new TestInputDTO("test_input_dto_name");

        TestDomain domain = new TestDomain("test_domain_name", 10);

        inputDTO.update(domain);

        assertThat(domain.getName(), equalTo("test_input_dto_name"));
        assertThat(domain.getAge(), equalTo(10));
    }

    @Test
    public void subConvertToTest() {
        SubTestInputDTO subTestInputDTO = new SubTestInputDTO();
        subTestInputDTO.setName("test_name");
        subTestInputDTO.setAge(10);

        TestDomain domain = subTestInputDTO.convertTo();

        assertThat(domain.getName(), equalTo("test_name"));
        assertThat(domain.getAge(), equalTo(10));
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
    public static class TestInputDTO implements InputConverter<TestDomain>, Serializable {

        private String name;
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubTestInputDTO extends TestInputDTO {

        private Integer age;
    }
}
