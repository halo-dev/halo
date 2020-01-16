package run.halo.app.utils;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author johnniang
 * @date 19-4-29
 */
public class JsonUtilsTest {

    @Test
    public void longConvertTest() throws IOException {
        long num = 10;

        String result = JsonUtils.objectToJson(num);

        assertEquals("10", result);

        num = JsonUtils.jsonToObject("10", Long.class);

        assertEquals(10, num);
    }
}