package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Time unit test
 *
 * @author johnniang
 * @date 19-4-29
 */
@Slf4j
public class TimeUnitTest {

    @Test
    public void convertTest() {
        Long millis = TimeUnit.DAYS.toMillis(30);

        log.debug("" + millis);
        log.debug("" + millis.intValue());
    }
}
