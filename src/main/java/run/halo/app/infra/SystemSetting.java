package run.halo.app.infra;

import lombok.Data;

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
        Boolean allowRegistration;
        String defaultRole;
    }

    @Data
    public static class Post {
        public static final String GROUP = "post";
        String sortOrder;
        Integer pageSize;
        Boolean review;
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
}
