package run.halo.app.utils;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author Chuntung Ho
 */
public class ThemeUtil {

    /**
     * Get current active theme from http request attributes.
     *
     * @return current active theme
     */
    public static String getTheme(HttpServletRequest request) {
        if (request == null) {
            request =
                ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
                    .getRequest();
        }
        ThemeResolver themeResolver = RequestContextUtils.getThemeResolver(request);
        String themeName = themeResolver.resolveThemeName(request);
        return themeName;
    }
}
