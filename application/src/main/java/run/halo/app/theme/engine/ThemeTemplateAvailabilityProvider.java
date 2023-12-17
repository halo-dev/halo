package run.halo.app.theme.engine;

import run.halo.app.theme.ThemeContext;

public interface ThemeTemplateAvailabilityProvider {

    boolean isTemplateAvailable(ThemeContext themeContext, String viewName);

}
