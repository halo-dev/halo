package run.halo.app.extension;

import org.springframework.util.StringUtils;

/**
 * Extension utilities.
 *
 * @author johnniang
 */
public final class ExtensionUtil {

    private ExtensionUtil() {
    }

    /**
     * Builds the name prefix of ExtensionStore.
     *
     * @param scheme is scheme of an Extension.
     * @return name prefix of ExtensionStore.
     */
    public static String buildStoreNamePrefix(Scheme scheme) {
        // rule of key: /registry/[group]/plural-name/extension-name
        StringBuilder builder = new StringBuilder("/registry/");
        if (StringUtils.hasText(scheme.groupVersionKind().group())) {
            builder.append(scheme.groupVersionKind().group()).append('/');
        }
        builder.append(scheme.plural());
        return builder.toString();
    }

    /**
     * Builds full name of ExtensionStore.
     *
     * @param scheme is scheme of an Extension.
     * @param name the exact name of Extension.
     * @return full name of ExtensionStore.
     */
    public static String buildStoreName(Scheme scheme, String name) {
        return buildStoreNamePrefix(scheme) + "/" + name;
    }

}
