package run.halo.app.utils;

import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Calendar;
import java.util.Date;

/**
 * Date utilities.
 *
 * @author johnniang
 * @date 3/18/19
 */
public class DateUtils {

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
}
