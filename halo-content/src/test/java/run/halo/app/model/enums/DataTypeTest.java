package run.halo.app.model.enums;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;

/**
 * Data type test.
 *
 * @author johnniang
 * @date 19-4-21
 */
public class DataTypeTest {

    @Test
    public void typeOf() {
        DataType type = DataType.typeOf("bool");
        System.out.println(type);
        assertThat(type, equalTo(DataType.BOOL));
    }
}