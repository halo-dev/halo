package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * DateUtils test.
 *
 * @author guqing
 * @date 2021-09-27
 */
@Slf4j
public class DateUtilsTest {

    private TimeZone defaultTimeZone;

    @BeforeEach
    public void setUp() {
        defaultTimeZone = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    @AfterEach
    public void cleanUp() {
        TimeZone.setDefault(defaultTimeZone);
    }

    @Test
    void testDateParse() {
        // yyyy-MM-dd HH:mm:ss
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "2021-09-26 06:39:12");

        // yyyy/MM/dd HH:mm:ss
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "2021/09/26 06:39:12");

        // yyyy.MM.dd HH:mm:ss
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "2021.09.26 06:39:12");

        // yyyy年MM月dd日 HH时mm分ss秒
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "2021年09月26日 06时39分12秒");

        // yyyy-MM-dd
        assertDateParseEquals("Sun Sep 26 00:00:00 CST 2021", "2021-09-26");

        // yyyy/MM/dd
        assertDateParseEquals("Sun Sep 26 00:00:00 CST 2021", "2021/09/26");

        // yyyy.MM.dd
        assertDateParseEquals("Sun Sep 26 00:00:00 CST 2021", "2021.09.26");

        // HH:mm:ss
        assertDateParseEquals("Thu Jan 01 06:39:12 CST 1970", "06:39:12");

        // HH时mm分ss秒
        assertDateParseEquals("Thu Jan 01 06:39:12 CST 1970", "06时39分12秒");

        // yyyy-MM-dd HH:mm
        assertDateParseEquals("Sun Sep 26 06:39:00 CST 2021", "2021-09-26 06:39");

        // yyyy-MM-dd HH:mm:ss.SSS
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "2021-09-26 06:39:12.015");

        // yyyyMMddHHmmss
        assertDateParseEquals("Sun Sep 26 06:39:12 CST 2021", "20210926063912");

        // yyyyMMddHHmmssSSS
        assertDateParseEquals("Sun Sep 26 09:59:15 CST 2021", "20210926063912015");

        // yyyyMMdd
        assertDateParseEquals("Sun Sep 26 00:00:00 CST 2021", "20210926");

        // yyyy-MM-dd'T'HH:mm:ss
        assertDateParseEquals("Sun Sep 26 10:09:10 CST 2021", "2021-09-26T10:09:10");

        // yyyy-MM-dd'T'HH:mm:ss'Z'
        assertDateParseEquals("Sun Sep 26 10:09:10 CST 2021", "2021-09-26T10:09:10Z");

        // yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
        assertDateParseEquals("Sun Sep 26 10:09:10 CST 2021", "2021-09-26T10:09:10.999Z");

        // yyyy-MM-dd'T'HH:mm:ssZ
        assertDateParseEquals("Sun Sep 26 05:34:31 CST 2021", "2021-09-26T05:34:31+0800");

        // yyyy-MM-dd'T'HH:mm:ss.SSSZ
        assertDateParseEquals("Sun Sep 26 05:34:31 CST 2021", "2021-09-26T05:34:31.999+0800");
    }

    @Test
    void testMonth() {
        GregorianCalendar january =
            new GregorianCalendar(2021, Calendar.JANUARY, 1);
        assertEquals(1, DateUtils.month(january.getTime()) + 1);

        GregorianCalendar february =
            new GregorianCalendar(2021, Calendar.FEBRUARY, 2);
        assertEquals(2, DateUtils.month(february.getTime()) + 1);

        GregorianCalendar december =
            new GregorianCalendar(2021, Calendar.DECEMBER, 31);
        assertEquals(12, DateUtils.month(december.getTime()) + 1);
    }

    @Test
    void testDayOfMonth() {
        GregorianCalendar january1st =
            new GregorianCalendar(2021, Calendar.JANUARY, 1);
        assertEquals(1, DateUtils.dayOfMonth(january1st.getTime()));

        GregorianCalendar january2nd =
            new GregorianCalendar(2021, Calendar.JANUARY, 2);
        assertEquals(2, DateUtils.dayOfMonth(january2nd.getTime()));

        GregorianCalendar january15th =
            new GregorianCalendar(2021, Calendar.JANUARY, 15);
        assertEquals(15, DateUtils.dayOfMonth(january15th.getTime()));

        GregorianCalendar january31th =
            new GregorianCalendar(2021, Calendar.JANUARY, 31);
        assertEquals(31, DateUtils.dayOfMonth(january31th.getTime()));
    }

    @Test
    void testYear() {
        GregorianCalendar year2015 =
            new GregorianCalendar(2015, Calendar.JANUARY, 1);
        assertEquals(2015, DateUtils.year(year2015.getTime()));

        GregorianCalendar year2019 =
            new GregorianCalendar(2019, Calendar.JANUARY, 1);
        assertEquals(2019, DateUtils.year(year2019.getTime()));

        GregorianCalendar year2021 =
            new GregorianCalendar(2021, Calendar.JANUARY, 1);
        assertEquals(2021, DateUtils.year(year2021.getTime()));
    }

    private void assertDateParseEquals(String expected, String dateStr) {
        log.debug("expected: {}, actual: {}, Locale: {}", expected,
            DateUtils.parseDate(dateStr, Locale.CHINESE), Locale.getDefault());
        assertEquals(expected, DateUtils.parseDate(dateStr, Locale.CHINESE).toString());
    }
}
