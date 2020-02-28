package run.halo.app.model.enums;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Data type test.
 *
 * @author johnniang
 * @date 19-4-21
 */
@Slf4j
public class DataTypeTest {

    @Test
    public void typeOf() {
        DataType type = DataType.typeOf("bool");
        log.debug("[{}]", type);
        assertThat(type, equalTo(DataType.BOOL));
    }
}