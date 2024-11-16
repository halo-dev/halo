package run.halo.app.theme.message;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import org.springframework.lang.Nullable;
import org.thymeleaf.exceptions.TemplateInputException;
import org.thymeleaf.exceptions.TemplateProcessingException;
import org.thymeleaf.util.StringUtils;
import run.halo.app.theme.ThemeContext;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeMessageResolutionUtils {

    private static final Map<String, String> EMPTY_MESSAGES = Collections.emptyMap();
    private static final String PROPERTIES_FILE_EXTENSION = ".properties";
    private static final String LOCATION = "i18n";

    @Nullable
    private static Reader messageReader(String messageResourceName, ThemeContext theme)
        throws FileNotFoundException {
        var themePath = theme.getPath();
        File messageFile = themePath.resolve(messageResourceName).toFile();
        if (!messageFile.exists()) {
            return null;
        }
        final InputStream inputStream = new FileInputStream(messageFile);
        return new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));
    }

    public static Map<String, String> resolveMessagesForTemplate(final Locale locale,
        ThemeContext theme) {

        // Compute all the resource names we should use: *_gl_ES-gheada.properties, *_gl_ES
        // .properties, _gl.properties...
        // The order here is important: as we will let values from more specific files
        // overwrite those in less specific,
        // (e.g. a value for gl_ES will have more precedence than a value for gl). So we will
        // iterate these resource
        // names from less specific to more specific.
        final List<String>
            messageResourceNames = computeMessageResourceNamesFromBase(locale);

        // Build the combined messages
        Map<String, String> combinedMessages = null;
        for (final String messageResourceName : messageResourceNames) {
            try {
                final Reader messageResourceReader = messageReader(messageResourceName, theme);
                if (messageResourceReader != null) {

                    final Properties messageProperties =
                        readMessagesResource(messageResourceReader);
                    if (messageProperties != null && !messageProperties.isEmpty()) {

                        if (combinedMessages == null) {
                            combinedMessages = new HashMap<>(20);
                        }

                        for (final Map.Entry<Object, Object> propertyEntry :
                            messageProperties.entrySet()) {
                            combinedMessages.put((String) propertyEntry.getKey(),
                                (String) propertyEntry.getValue());
                        }

                    }

                }

            } catch (final IOException ignored) {
                // File might not exist, simply try the next one
            }
        }

        if (combinedMessages == null) {
            return EMPTY_MESSAGES;
        }

        return Collections.unmodifiableMap(combinedMessages);
    }

    private static List<String> computeMessageResourceNamesFromBase(final Locale locale) {

        final List<String> resourceNames = new ArrayList<>(5);

        if (StringUtils.isEmptyOrWhitespace(locale.getLanguage())) {
            throw new TemplateProcessingException(
                "Locale \"" + locale + "\" "
                    + "cannot be used as it does not specify a language.");
        }

        resourceNames.add(getResourceName("default"));
        resourceNames.add(getResourceName(locale.getLanguage()));

        if (!StringUtils.isEmptyOrWhitespace(locale.getCountry())) {
            resourceNames.add(
                getResourceName(locale.getLanguage() + "_" + locale.getCountry()));
        }

        if (!StringUtils.isEmptyOrWhitespace(locale.getVariant())) {
            resourceNames.add(getResourceName(
                locale.getLanguage() + "_" + locale.getCountry() + "-" + locale.getVariant()));
        }

        return resourceNames;

    }

    private static String getResourceName(String name) {
        return LOCATION + "/" + name + PROPERTIES_FILE_EXTENSION;
    }


    private static Properties readMessagesResource(final Reader propertiesReader) {
        if (propertiesReader == null) {
            return null;
        }
        final Properties properties = new Properties();
        try (propertiesReader) {
            // Note Properties#load(Reader) this is JavaSE 6 specific, but Thymeleaf 3.0 does
            // not support Java 5 anymore...
            properties.load(propertiesReader);
        } catch (final Exception e) {
            throw new TemplateInputException("Exception loading messages file", e);
        }
        // ignore errors closing
        return properties;
    }

}
