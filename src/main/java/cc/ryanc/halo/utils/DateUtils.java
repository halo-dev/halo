package cc.ryanc.halo.utils;

import org.springframework.lang.NonNull;

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
}
