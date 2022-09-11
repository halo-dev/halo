package run.halo.app.theme.finders.vo;

import java.util.Map;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.With;
import run.halo.app.core.extension.Theme;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Theme}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
@ToString
public class ThemeVo {

    MetadataOperator metadata;

    Theme.ThemeSpec spec;

    @With
    Map<String, Object> config;

    /**
     * Convert {@link Theme} to {@link ThemeVo}.
     *
     * @param theme theme extension
     * @return theme value object
     */
    public static ThemeVo from(Theme theme) {
        return ThemeVo.builder()
            .metadata(theme.getMetadata())
            .spec(theme.getSpec())
            .config(Map.of())
            .build();
    }
}
