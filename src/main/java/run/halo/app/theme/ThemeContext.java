package run.halo.app.theme;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@EqualsAndHashCode(of = "name")
public class ThemeContext {

    public static final String THEME_PREVIEW_PARAM_NAME = "preview-theme";

    private String name;

    private Path path;

    private boolean active;
}
