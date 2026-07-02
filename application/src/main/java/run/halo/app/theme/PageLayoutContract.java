package run.halo.app.theme;

import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/** Constants and helpers for the page layout contract. */
public final class PageLayoutContract {

    public static final String TEMPLATE_NAME = "layout";
    public static final String TEMPLATE_FILE = "templates/layout.html";
    public static final String FRAGMENT_NAME = "html";

    private static final Pattern PLUGIN_TEMPLATE_PATTERN = Pattern.compile("plugin:([A-Za-z0-9\\-.]+):(.+)");

    private PageLayoutContract() {}

    public static boolean isContractTemplate(String template) {
        return TEMPLATE_NAME.equals(template) || "layout.html".equals(template);
    }

    public static boolean isPluginOwnedTemplate(String ownerTemplate) {
        return StringUtils.isNotBlank(ownerTemplate)
                && PLUGIN_TEMPLATE_PATTERN.matcher(ownerTemplate).matches();
    }
}
