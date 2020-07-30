package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * Time unit test
 *
 * @author johnniang
 * @date 19-4-29
 */
@Slf4j
class TimeUnitTest {

    @Test
    void convertTest() {
        long millis = TimeUnit.DAYS.toMillis(30);

        log.debug("" + millis);
        log.debug("" + (int) millis);
    }
}
