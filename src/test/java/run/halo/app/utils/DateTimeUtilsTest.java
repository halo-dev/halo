package run.halo.app.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.junit.jupiter.api.Test;

/**
 * DateTimeUtils 测试用例.
 *
 * @author LeiXinXin
 * @author guqing
 * @date 2020/1/9
 */
class DateTimeUtilsTest {

    /**
     * 获取上海时区的当前时间.
     */
    @Test
    void nowTest() {
        final LocalDateTime now = DateTimeUtils.now().withNano(0);
        assertNotNull(now);
        final LocalDateTime ctt =
            DateTimeUtils.now(ZoneId.of(ZoneId.SHORT_IDS.get("CTT"))).withNano(0);
        assertNotNull(ctt);

        assertEquals(0, Duration.between(now, ctt).toMinutes());
    }

    /**
     * 格式化日期时间.
     */
    @Test
    void formatTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 9, 20, 20, 20);
        final String formatDate = DateTimeUtils.formatDate(now);
        assertEquals("20200109", formatDate);

        final LocalDate localDate = LocalDate.of(2020, 1, 9);
        final String localFormatDate = DateTimeUtils.formatDate(localDate);
        assertEquals("20200109", localFormatDate);

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final String formatDateTime1 = DateTimeUtils.format(now, dateTimeFormatter);
        assertEquals("2020/01/09", formatDateTime1);

        final LocalTime time1 = LocalTime.of(10, 20);
        final String formatTime = DateTimeUtils.formatTime(time1);
        assertEquals("1020", formatTime);

        final String formatTime1 =
            DateTimeUtils.format(time1, DateTimeFormatter.ofPattern("HH:mm"));
        assertEquals("10:20", formatTime1);
    }

    /**
     * 增加时间.
     */
    @Test
    void plusTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 9, 10, 20);
        final LocalDateTime localDateTime = DateTimeUtils.plusOneMinute(now);
        assertEquals("2020-01-09T10:21", localDateTime.toString());

        LocalTime localTime = LocalTime.of(7, 30);
        final LocalDateTime localDateTime1 = DateTimeUtils.plusOneMinute(now, localTime);
        assertEquals("2020-01-09T07:31", localDateTime1.toString());

        final LocalDate date = LocalDate.of(2020, 1, 3);
        final LocalDateTime localDateTime2 = DateTimeUtils.plusOneMinute(date, localTime);
        assertEquals("2020-01-03T07:31", localDateTime2.toString());

        final LocalTime localTime1 = DateTimeUtils.plusOneMinute(localTime);
        assertEquals("07:31", localTime1.toString());

        final LocalDateTime localDateTime3 = DateTimeUtils.plusDays(now, 10);
        assertEquals("2020-01-19T10:20", localDateTime3.toString());
    }

    /**
     * 解析时间格式为LocalDateTime.
     */
    @Test
    void parseTest() {
        String time = "20200109135500000";
        final LocalDateTime localDateTime = DateTimeUtils.parseCttDateTime(time);
        assertEquals("2020-01-09T13:55", localDateTime.toString());

        String time2 = "2020/1/9 13:56";
        final LocalDateTime dateTime =
            DateTimeUtils.parse(time2, DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"));
        assertEquals("2020-01-09T13:56", dateTime.toString());
    }

    /**
     * 减少日期时间.
     */
    @Test
    void minusTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 3, 14, 20);
        final LocalDateTime localDateTime = DateTimeUtils.minusOneMinutes(now);
        assertEquals("2020-01-03T14:19", localDateTime.toString());
    }

    /**
     * 转为Instant.
     */
    @Test
    void toInstantTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 3, 14, 20);

        final Instant instant = DateTimeUtils.toInstant(now);
        assertEquals("2020-01-03T06:20:00Z", instant.toString());

        final Instant jst = DateTimeUtils.toInstant(now, ZoneId.of(ZoneId.SHORT_IDS.get("JST")));
        assertEquals("2020-01-03T05:20:00Z", jst.toString());
    }

    /**
     * 一些其他的使用方法.
     */
    @Test
    void other() {
        LocalDateTime startInclusive = LocalDateTime.of(2020, 1, 3, 10, 10, 30);
        LocalDateTime endInclusive = LocalDateTime.of(2020, 1, 4, 10, 10, 30);
        // End 大于等于 Start
        final boolean greaterOrEqual = DateTimeUtils.isGreaterOrEqual(startInclusive, endInclusive);
        assertTrue(greaterOrEqual);

        /*
         * 小于等于
         */
        final boolean lessThanOrEqual =
            DateTimeUtils.isLessThanOrEqual(startInclusive, endInclusive);
        assertFalse(lessThanOrEqual);

        /*
         * 两个时间的比较是否为0
         */
        final boolean zero = DateTimeUtils.isZero(startInclusive, endInclusive);
        assertFalse(zero);

        // 是否是负数，startInclusive 大于 endInclusive 就是负数
        final boolean negative = DateTimeUtils.isNegative(startInclusive, endInclusive);
        assertFalse(negative);

        // 是否是中午
        final boolean noon = DateTimeUtils.isNoon(LocalTime.of(12, 0));
        assertTrue(noon);

        // 把纳秒和秒设置为0
        final LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 5, 6, 40, 30, 999);
        final LocalDateTime time = DateTimeUtils.secondAndNanoSetZero(localDateTime);
        assertEquals("2020-01-05T06:40", time.toString());
    }

    @Test
    void toLocalDateTime() {
        GregorianCalendar january31th =
            new GregorianCalendar(2021, Calendar.JANUARY, 31, 12, 45, 20);
        LocalDateTime localDateTime = DateTimeUtils.toLocalDateTime(january31th.getTime());
        assertEquals("2021-01-31T12:45:20", localDateTime.toString());
    }
}