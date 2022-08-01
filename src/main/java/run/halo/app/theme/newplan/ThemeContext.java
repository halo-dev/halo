package run.halo.app.theme.newplan;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(of = "name")
@Builder
public class ThemeContext {

    private String name;

    private String path;

    private boolean active;
}
