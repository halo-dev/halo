package run.halo.app.theme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

/**
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
public class ThemeContextHolder {
    private static final ThreadLocal<ThemeContext> themeContextHolder =
        new NamedThreadLocal<>("Theme context");

    public static void resetThemeContext() {
        log.debug("Reset theme context.");
        themeContextHolder.remove();
    }

    public static void setThemeContext(@Nullable ThemeContext themeContext) {
        themeContextHolder.set(themeContext);
    }

    public static ThemeContext getThemeContext() {
        ThemeContext themeContext = themeContextHolder.get();
        if (themeContext == null) {
            throw new IllegalStateException("No thread-bound request found: "
                + "Are you referring to theme context outside of an actual web request, "
                + "or processing a request outside of the originally receiving thread? "
                + "If you are actually operating within a web request and still receive this "
                + "message, "
                + "your code is probably running outside of DispatcherServlet: "
                + "In this case, use ThemeContextBoundariesProcessor or ThemeContextFilter to "
                + "expose the current request.");
        }
        return themeContext;
    }

}
