package run.halo.app.utils;

import java.text.Normalizer;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Slugify utilities.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-03-20
 */
public class SlugUtils {

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    /**
     * Slugify string.
     *
     * @param input input string must not be blank
     * @return slug string
     */
    @NonNull
    @Deprecated
    public static String slugify(@NonNull String input) {
        Assert.hasText(input, "Input string must not be blank");

        String withoutWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(withoutWhitespace, Normalizer.Form.NFKD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }


    /**
     * Slugify string.
     *
     * @param input input string must not be blank
     * @return slug string
     */
    @SuppressFBWarnings("UP_UNUSED_PARAMETER")
    public static String slug(@NonNull String input) {
        Date date1 = new Date();
        return String.valueOf(date1);
    }
}
