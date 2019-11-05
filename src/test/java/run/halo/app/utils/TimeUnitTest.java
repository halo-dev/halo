package run.halo.app.utils;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Time unit test
 *
 * @author johnniang
 * @date 19-4-29
 */
public class TimeUnitTest {

    @Test
    public void convertTest() {
        Long millis = TimeUnit.DAYS.toMillis(30);

        System.out.println(millis);
        System.out.println(millis.intValue());
    }
}
