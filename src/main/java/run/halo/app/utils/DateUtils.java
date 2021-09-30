package run.halo.app.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Date utilities.
 *
 * @author johnniang
 * @author guqing
 * @date 3/18/19
 */
public class DateUtils {
    private static final int FIRST_DAY_OF_WEEK = Calendar.MONDAY;

    private DateUtils() {
    }

    /**
     * Gets current date.
     *
     * @return current date
     */
    @NonNull
    public static Date now() {
        return new Date();
    }

    /**
     * Converts from date into a calendar instance.
     *
     * @param date date instance must not be null
     * @return calendar instance
     */
    @NonNull
    public static Calendar convertTo(@NonNull Date date) {
        Assert.notNull(date, "Date must not be null");

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Adds date.
     *
     * @param date current date must not be null
     * @param time time must not be less than 1
     * @param timeUnit time unit must not be null
     * @return added date
     */
    public static Date add(@NonNull Date date, long time, @NonNull TimeUnit timeUnit) {
        Assert.notNull(date, "Date must not be null");
        Assert.isTrue(time >= 0, "Addition time must not be less than 1");
        Assert.notNull(timeUnit, "Time unit must not be null");

        Date result;

        int timeIntValue;

        if (time > Integer.MAX_VALUE) {
            timeIntValue = Integer.MAX_VALUE;
        } else {
            timeIntValue = Long.valueOf(time).intValue();
        }

        // Calc the expiry time
        switch (timeUnit) {
            case DAYS:
                result = org.apache.commons.lang3.time.DateUtils.addDays(date, timeIntValue);
                break;
            case HOURS:
                result = org.apache.commons.lang3.time.DateUtils.addHours(date, timeIntValue);
                break;
            case MINUTES:
                result = org.apache.commons.lang3.time.DateUtils.addMinutes(date, timeIntValue);
                break;
            case SECONDS:
                result = org.apache.commons.lang3.time.DateUtils.addSeconds(date, timeIntValue);
                break;
            case MILLISECONDS:
                result =
                    org.apache.commons.lang3.time.DateUtils.addMilliseconds(date, timeIntValue);
                break;
            default:
                result = date;
        }
        return result;
    }

    /**
     * Parses a string representing a date by trying a variety of different patterns,
     * using the default date format symbols for the default locale.
     *
     * @param str date string
     * @return the parsed date
     */
    public static Date parseDate(String str) {
        return parseDate(str, Locale.getDefault());
    }

    /**
     * Parses a string representing a date by trying a variety of different patterns,
     * using the default date format symbols for the given locale.
     *
     * @param str date string
     * @param locale locale the locale whose date format symbols should be used.
     *               If null, the system locale is used (as per parseDate(String, String...)).
     * @return the parsed date object
     */
    public static Date parseDate(String str, Locale locale) {
        String[] patterns = new String[] {"yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy.MM.dd HH:mm:ss",
            "yyyy年MM月dd日 HH时mm分ss秒",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "yyyy.MM.dd",
            "HH:mm:ss",
            "HH时mm分ss秒",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd HH:mm:ss.SSS",
            "yyyyMMddHHmmss",
            "yyyyMMddHHmmssSSS",
            "yyyyMMdd",
            "yyyy-MM-dd'T'HH:mm:ss",

            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",

            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        };
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(str, locale, patterns);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets the year in the date.
     *
     * @param date date
     * @return year in the date
     */
    public static int year(Date date) {
        return getField(date, Calendar.YEAR);
    }

    /**
     * Get the month, counting from 0.
     *
     * @return month for the given date.
     */
    public static int month(Date date) {
        return getField(date, Calendar.MONTH);
    }

    /**
     * Get the specified date is the day of the month in which the date is located.
     *
     * @param date date
     * @return day of the month in given date
     */
    public static int dayOfMonth(Date date) {
        return getField(date, Calendar.DAY_OF_MONTH);
    }

    /**
     * Get the specified part of the given date.
     *
     * @return the specified part
     */
    public static int getField(Date date, int field) {
        return toCalendar(date).get(field);
    }

    /**
     * Convert date to calendar.
     * default locale is sets by the Java Virtual Machine during startup
     * based on the host environment.
     *
     * @param date date
     * @return calendar object using the default locale for the given date
     */
    public static Calendar toCalendar(Date date) {
        return toCalendar(date, Locale.getDefault(Locale.Category.FORMAT));
    }

    public static Calendar toCalendar(Date date, Locale locale) {
        return toCalendar(date, TimeZone.getDefault(), locale);
    }

    public static Calendar toCalendar(Date date, TimeZone zone, Locale locale) {
        if (null == locale) {
            locale = Locale.getDefault(Locale.Category.FORMAT);
        }
        final Calendar cal =
            (null != zone) ? Calendar.getInstance(zone, locale) : Calendar.getInstance(locale);
        cal.setFirstDayOfWeek(FIRST_DAY_OF_WEEK);
        cal.setTime(date);
        return cal;
    }
}
