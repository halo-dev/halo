package run.halo.app.utils;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * DateTimeUtils 测试用例
 *
 * @author LeiXinXin
 * @date 2020/1/9
 */
@RunWith(SpringRunner.class)
public class DateTimeUtilsTest {
    /**
     * 获取上海时区的当前时间
     */
    @Test
    public void nowTest() {
        final LocalDateTime now = DateTimeUtils.now().withNano(0);
        Assert.assertNotNull(now);
        final LocalDateTime ctt = DateTimeUtils.now(ZoneId.of(ZoneId.SHORT_IDS.get("CTT"))).withNano(0);
        Assert.assertNotNull(ctt);

        Assert.assertEquals(Duration.between(now, ctt).toMinutes(), 0);
    }

    /**
     * 格式化日期时间
     */
    @Test
    public void formatTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 9, 20, 20, 20);
        final String formatDate = DateTimeUtils.formatDate(now);
        Assert.assertEquals(formatDate, "20200109");

        final LocalDate localDate = LocalDate.of(2020, 1, 9);
        final String localFormatDate = DateTimeUtils.formatDate(localDate);
        Assert.assertEquals(localFormatDate, "20200109");

        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        final String formatDateTime1 = DateTimeUtils.format(now, dateTimeFormatter);
        Assert.assertEquals(formatDateTime1, "2020/01/09");

        final LocalTime time1 = LocalTime.of(10, 20);
        final String formatTime = DateTimeUtils.formatTime(time1);
        Assert.assertEquals(formatTime, "1020");

        final String formatTime1 = DateTimeUtils.format(time1, DateTimeFormatter.ofPattern("HH:mm"));
        Assert.assertEquals(formatTime1, "10:20");
    }

    /**
     * 增加时间
     */
    @Test
    public void plusTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 9, 10, 20);
        final LocalDateTime localDateTime = DateTimeUtils.plusOneMinute(now);
        Assert.assertEquals(localDateTime.toString(), "2020-01-09T10:21");

        LocalTime localTime = LocalTime.of(7, 30);
        final LocalDateTime localDateTime1 = DateTimeUtils.plusOneMinute(now, localTime);
        Assert.assertEquals(localDateTime1.toString(), "2020-01-09T07:31");

        final LocalDate date = LocalDate.of(2020, 1, 3);
        final LocalDateTime localDateTime2 = DateTimeUtils.plusOneMinute(date, localTime);
        Assert.assertEquals(localDateTime2.toString(), "2020-01-03T07:31");

        final LocalTime localTime1 = DateTimeUtils.plusOneMinute(localTime);
        Assert.assertEquals(localTime1.toString(), "07:31");

        final LocalDateTime localDateTime3 = DateTimeUtils.plusDays(now, 10);
        Assert.assertEquals(localDateTime3.toString(), "2020-01-19T10:20");
    }

    /**
     * 解析时间格式为LocalDateTime
     */
    @Test
    public void parseTest() {
        String time = "20200109135500000";
        final LocalDateTime localDateTime = DateTimeUtils.parseCttDateTime(time);
        Assert.assertEquals(localDateTime.toString(), "2020-01-09T13:55");

        String time2 = "2020/1/9 13:56";
        final LocalDateTime dateTime = DateTimeUtils.parse(time2, DateTimeFormatter.ofPattern("yyyy/M/d HH:mm"));
        Assert.assertEquals(dateTime.toString(), "2020-01-09T13:56");
    }

    /**
     * 减少日期时间
     */
    @Test
    public void minusTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 3, 14, 20);
        final LocalDateTime localDateTime = DateTimeUtils.minusOneMinutes(now);
        Assert.assertEquals(localDateTime.toString(), "2020-01-03T14:19");
    }

    /**
     * 转为Instant
     */
    @Test
    public void toInstantTest() {
        final LocalDateTime now = LocalDateTime.of(2020, 1, 3, 14, 20);

        final Instant instant = DateTimeUtils.toInstant(now);
        Assert.assertEquals(instant.toString(), "2020-01-03T06:20:00Z");

        final Instant jst = DateTimeUtils.toInstant(now, ZoneId.of(ZoneId.SHORT_IDS.get("JST")));
        Assert.assertEquals(jst.toString(), "2020-01-03T05:20:00Z");
    }

    /**
     * 一些其他的使用方法
     */
    @Test
    public void other() {
        LocalDateTime startInclusive = LocalDateTime.of(2020, 1, 3, 10, 10, 30);
        LocalDateTime endInclusive = LocalDateTime.of(2020, 1, 4, 10, 10, 30);
        // End 大于等于 Start
        final boolean greaterOrEqual = DateTimeUtils.isGreaterOrEqual(startInclusive, endInclusive);
        Assert.assertTrue(greaterOrEqual);

        /*
         * 小于等于
         */
        final boolean lessThanOrEqual = DateTimeUtils.isLessThanOrEqual(startInclusive, endInclusive);
        Assert.assertFalse(lessThanOrEqual);

        /*
         * 两个时间的比较是否为0
         */
        final boolean zero = DateTimeUtils.isZero(startInclusive, endInclusive);
        Assert.assertFalse(zero);

        // 是否是负数，startInclusive 大于 endInclusive 就是负数
        final boolean negative = DateTimeUtils.isNegative(startInclusive, endInclusive);
        Assert.assertFalse(negative);

        // 是否是中午
        final boolean noon = DateTimeUtils.isNoon(LocalTime.of(12, 0));
        Assert.assertTrue(noon);

        // 把纳秒和秒设置为0
        final LocalDateTime localDateTime = LocalDateTime.of(2020, 1, 5, 6, 40, 30, 999);
        final LocalDateTime time = DateTimeUtils.secondAndNanoSetZero(localDateTime);
        Assert.assertEquals(time.toString(), "2020-01-05T06:40");
    }
}