package run.halo.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Local date time test.
 *
 * @author johnniang
 */
@Slf4j
public class LocalDateTimeTest {

    @Test
    public void dateTimeToStringTest() {
        LocalDateTime dateTime = LocalDateTime.now();
        log.debug(dateTime.toString());
        log.debug(dateTime.toLocalDate().toString());
        String DATE_FORMATTER = "yyyy-MM-dd-HH-mm-ss-";
        log.debug(dateTime.format(DateTimeFormatter.ofPattern(DATE_FORMATTER)));
    }

}
