package run.halo.app.theme.utils;

import static org.apache.commons.lang3.StringUtils.prependIfMissing;
import static org.apache.commons.lang3.StringUtils.removeEnd;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;

/**
 * Pattern utility methods.
 *
 * @author johnniang
 * @since 2.22.0
 */
public enum PatternUtils {
    ;

    /**
     * Normalize the pattern by ensuring it starts with a "/" and does not end with a "/".
     *
     * @param pattern the pattern to normalize, must not be blank or just "/"
     * @return the normalized pattern
     */
    public static String normalizePattern(String pattern) {
        Assert.hasText(pattern, "Pattern must not be blank");
        Assert.isTrue(!"/".equals(pattern.trim()), "Pattern must not be just '/'");
        pattern = prependIfMissing(pattern.trim(), "/");
        return removeEnd(pattern, "/");
    }


    /**
     * Normalize the post pattern, if the post pattern starts with /archives/ or /categories/,
     * replace it with the corresponding pattern from rules.
     *
     * @param rules the theme route rules
     * @return the normalized post pattern
     */
    public static String normalizePostPattern(ThemeRouteRules rules) {
        var postPattern = normalizePattern(rules.getPost());
        if (StringUtils.startsWith(postPattern, "/archives/")) {
            var archivesPattern = normalizePattern(rules.getArchives());
            postPattern = archivesPattern + StringUtils.removeStart(postPattern, "/archives");
        } else if (StringUtils.startsWith(postPattern, "/categories/")) {
            var categoriesPattern = normalizePattern(rules.getCategories());
            postPattern = categoriesPattern + StringUtils.removeStart(postPattern, "/categories");
        }
        return postPattern;
    }
}
