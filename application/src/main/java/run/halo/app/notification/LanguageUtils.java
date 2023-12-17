package run.halo.app.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * Language utils to help us to compute the language.
 *
 * @author guqing
 * @since 2.10.0
 */
@UtilityClass
public class LanguageUtils {

    /**
     * Compute all the possible languages we should use: *_gl_ES-gheada, *_gl_ES, _gl... from a
     * given locale.
     * The first element of the list is "default" if it can not find the language, use default.
     *
     * @param locale locale
     * @return list of possible languages, from less specific to more specific.
     */
    public static List<String> computeLangFromLocale(Locale locale) {
        final List<String> resourceNames = new ArrayList<>(5);

        if (StringUtils.isBlank(locale.getLanguage())) {
            throw new IllegalArgumentException(
                "Locale \"" + locale + "\" "
                    + "cannot be used as it does not specify a language.");
        }

        resourceNames.add("default");
        resourceNames.add(locale.getLanguage());

        if (StringUtils.isNotBlank(locale.getCountry())) {
            resourceNames.add(locale.getLanguage() + "_" + locale.getCountry());
        }

        if (StringUtils.isNotBlank(locale.getVariant())) {
            resourceNames.add(
                locale.getLanguage() + "_" + locale.getCountry() + "-" + locale.getVariant());
        }
        return resourceNames;
    }
}
