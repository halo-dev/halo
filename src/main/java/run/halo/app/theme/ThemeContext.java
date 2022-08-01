package run.halo.app.theme;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Data;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
public class ThemeContext {
    public static final String THEME_CONTEXT_KEY = ThemeContext.class.getName() + ".CONTEXT";

    private String themeName;

    private Path path;

    private boolean isActive;
}
