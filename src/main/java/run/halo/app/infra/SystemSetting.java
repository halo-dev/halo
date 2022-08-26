package run.halo.app.infra;

import lombok.Data;

/**
 * TODO Optimization value acquisition.
 *
 * @author guqing
 * @since 2.0.0
 */
public class SystemSetting {

    @Data
    public static class Theme {
        public static final String GROUP = "theme";

        private String active;

        private ThemeRouteRules routeRules;
    }

    @Data
    public static class ThemeRouteRules {
        private String categories;
        private String archives;
        private String post;
        private String tags;
    }
}
