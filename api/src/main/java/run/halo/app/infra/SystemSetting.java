package run.halo.app.infra;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.convert.ApplicationConversionService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.infra.utils.JsonUtils;

/**
 * TODO Optimization value acquisition.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SystemSetting {
    public static final String SYSTEM_CONFIG_DEFAULT = "system-default";
    public static final String SYSTEM_CONFIG = "system";

    @Data
    public static class Theme {
        public static final String GROUP = "theme";

        private String active;
    }

    @Data
    public static class ThemeRouteRules {
        public static final String GROUP = "routeRules";

        private String categories;
        private String archives;
        private String post;
        private String tags;

        public static ThemeRouteRules empty() {
            ThemeRouteRules rules = new ThemeRouteRules();
            rules.setPost("/archives/{slug}");
            rules.setArchives("/archives");
            rules.setTags("/tags");
            rules.setCategories("/categories");
            return rules;
        }
    }

    @Data
    public static class CodeInjection {
        public static final String GROUP = "codeInjection";

        private String globalHead;

        private String contentHead;

        private String footer;
    }

    @Data
    public static class Basic {
        public static final String GROUP = "basic";
        String title;
        String subtitle;
        String logo;
        String favicon;
    }

    @Data
    public static class User {
        public static final String GROUP = "user";
        boolean allowRegistration;
        boolean mustVerifyEmailOnRegistration;
        String defaultRole;
        String avatarPolicy;
        String ucAttachmentPolicy;
    }

    @Data
    public static class Post {
        public static final String GROUP = "post";
        Integer postPageSize;
        Integer archivePageSize;
        Integer categoryPageSize;
        Integer tagPageSize;
        Boolean review;
        String slugGenerationStrategy;

        String attachmentPolicyName;
        String attachmentGroupName;
    }

    @Data
    public static class Seo {
        public static final String GROUP = "seo";
        Boolean blockSpiders;
        String keywords;
        String description;
    }

    @Data
    public static class Comment {
        public static final String GROUP = "comment";
        Boolean enable;
        Boolean requireReviewForNew;
        Boolean systemUserOnly;
    }

    @Data
    public static class Menu {
        public static final String GROUP = "menu";
        public String primary;
    }

    @Data
    public static class AuthProvider {
        public static final String GROUP = "authProvider";
        /**
         * Currently keep it to be compatible with the reference of the plugin.
         *
         * @deprecated Use {@link #getStates()} instead.
         */
        @Deprecated(since = "2.20.0", forRemoval = true)
        private Set<String> enabled;

        private List<AuthProviderState> states;

        /**
         * <p>To be compatible with the old version of the enabled field and retained,
         * since 2.20.0 version, we uses the states field, so the data needs to be synchronized
         * to the enabled field, and this method needs to be deleted when the enabled field is
         * removed.</p>
         *
         * @deprecated Use {@link #getStates()} instead.
         */
        @Deprecated(since = "2.20.0", forRemoval = true)
        public Set<String> getEnabled() {
            if (states == null) {
                return enabled;
            }
            return this.states.stream()
                .filter(AuthProviderState::isEnabled)
                .map(AuthProviderState::getName)
                .collect(Collectors.toSet());
        }
    }

    @Data
    @Accessors(chain = true)
    public static class AuthProviderState {
        private String name;
        private boolean enabled;
        private int priority;
    }

    /**
     * ExtensionPointEnabled key is metadata name of extension point and value is a list of
     * extension definition names.
     */
    public static class ExtensionPointEnabled extends LinkedHashMap<String, LinkedHashSet<String>> {

        public static final String GROUP = "extensionPointEnabled";

    }

    public static <T> T get(ConfigMap configMap, String key, Class<T> type) {
        var data = configMap.getData();
        var valueString = data.get(key);
        if (valueString == null) {
            return null;
        }
        var conversionService = ApplicationConversionService.getSharedInstance();
        if (conversionService.canConvert(String.class, type)) {
            return conversionService.convert(valueString, type);
        }
        return JsonUtils.jsonToObject(valueString, type);
    }
}
