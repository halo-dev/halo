package run.halo.app.theme;

import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ThemeResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Theme utility.
 *
 * @author Chuntung Ho
 */
public class ThemeUtil {
    public ThemeUtil() {
    }

    /**
     * Append current theme prefix to path.
     * <p>
     * e.g. /abc/dd -> /themes/currentTheme/abc/dd
     *
     * @param path
     * @return
     */
    public String url(String path) {
        return "/themes/" + getTheme() + (path.startsWith("/") ? "" : "/") + path;
    }

    /**
     * Insert size between file name and ext.
     * <p>
     * e.g.: path.jpg -> path-size.jpg
     *
     * @param path
     * @param size
     * @return
     */
    public String imgUrl(String path, String size) {
        int i = path.lastIndexOf('.');
        if (i == -1) {
            return path + '-' + size;
        }
        String prefix = path.substring(0, i);
        String ext = path.substring(i);
        return prefix + '-' + size + ext;
    }

    /**
     * Get current active theme from http request attributes.
     *
     * @return
     */
    public static String getTheme() {
        HttpServletRequest request =
            ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes()))
                .getRequest();
        ThemeResolver themeResolver = RequestContextUtils.getThemeResolver(request);
        String themeName = themeResolver.resolveThemeName(request);
        return themeName;
    }

}
