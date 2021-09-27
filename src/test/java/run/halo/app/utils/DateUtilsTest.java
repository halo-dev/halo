package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * @author guqing
 * @since 2021-09-27
 */
public class DateUtilsTest {

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
        assertEquals(1 , DateUtils.month(january.getTime()) + 1);

        GregorianCalendar february =
            new GregorianCalendar(2021, Calendar.FEBRUARY, 2);
        assertEquals(2 , DateUtils.month(february.getTime()) + 1);

        GregorianCalendar december =
            new GregorianCalendar(2021, Calendar.DECEMBER, 30);
        assertEquals(12 , DateUtils.month(december.getTime()) + 1);
    }

    private void assertDateParseEquals(String expected, String dateStr) {
        assertEquals(expected, DateUtils.parseDate(dateStr, Locale.CHINA).toString());
    }
}
