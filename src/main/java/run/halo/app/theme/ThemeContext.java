package run.halo.app.theme;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>如果当前激活主题为 <code>default</code> 那么先访问 /post?preview-theme=default
 * 再访问 /post 应该是不同的 {@link ThemeContext}.
 * 因此 <code>EqualsAndHashCode</code> 要比较 <code>name</code> 和 <code>active</code></p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@EqualsAndHashCode(of = {"name", "active"})
public class ThemeContext {

    public static final String THEME_PREVIEW_PARAM_NAME = "preview-theme";

    private String name;

    private Path path;

    private boolean active;
}
