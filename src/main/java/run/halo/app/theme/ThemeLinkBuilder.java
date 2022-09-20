package run.halo.app.theme;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.StandardLinkBuilder;
import run.halo.app.infra.utils.PathUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
public class ThemeLinkBuilder extends StandardLinkBuilder {
    public static final String THEME_ASSETS_PREFIX = "/assets";
    public static final String THEME_PREVIEW_PREFIX = "/themes";

    private final ThemeContext theme;

    public ThemeLinkBuilder(ThemeContext theme) {
        this.theme = theme;
    }

    @Override
    protected String processLink(IExpressionContext context, String link) {
        if (link == null || isLinkBaseAbsolute(link)) {
            return link;
        }

        if (StringUtils.isBlank(link)) {
            link = "/";
        }

        if (isAssetsRequest(link)) {
            return PathUtils.combinePath(THEME_PREVIEW_PREFIX, theme.getName(), link);
        }

        // not assets link
        if (theme.isActive()) {
            return link;
        }

        return UriComponentsBuilder.fromUriString(link)
            .queryParam(ThemeContext.THEME_PREVIEW_PARAM_NAME, theme.getName())
            .build().toString();
    }

    private static boolean isLinkBaseAbsolute(final CharSequence linkBase) {
        final int linkBaseLen = linkBase.length();
        if (linkBaseLen < 2) {
            return false;
        }
        final char c0 = linkBase.charAt(0);
        if (c0 == 'm' || c0 == 'M') {
            // Let's check for "mailto:"
            if (linkBase.length() >= 7
                && Character.toLowerCase(linkBase.charAt(1)) == 'a'
                && Character.toLowerCase(linkBase.charAt(2)) == 'i'
                && Character.toLowerCase(linkBase.charAt(3)) == 'l'
                && Character.toLowerCase(linkBase.charAt(4)) == 't'
                && Character.toLowerCase(linkBase.charAt(5)) == 'o'
                && Character.toLowerCase(linkBase.charAt(6)) == ':') {
                return true;
            }
        } else if (c0 == '/') {
            return linkBase.charAt(1)
                == '/'; // It starts with '//' -> true, any other '/x' -> false
        }
        for (int i = 0; i < (linkBaseLen - 2); i++) {
            // Let's try to find the '://' sequence anywhere in the base --> true
            if (linkBase.charAt(i) == ':' && linkBase.charAt(i + 1) == '/'
                && linkBase.charAt(i + 2) == '/') {
                return true;
            }
        }
        return false;
    }

    private boolean isAssetsRequest(String link) {
        return link.startsWith(THEME_ASSETS_PREFIX);
    }
}
