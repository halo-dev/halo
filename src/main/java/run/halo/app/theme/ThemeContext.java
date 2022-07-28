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

    private String themeName;

    private Path path;

    private boolean isActive;
}
