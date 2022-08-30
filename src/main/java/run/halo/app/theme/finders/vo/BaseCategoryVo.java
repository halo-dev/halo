package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Base class for category value object.
 *
 * @author guqing
 * @since 2.0.0
 */
@Getter
@SuperBuilder
public class BaseCategoryVo {
    String name;

    String displayName;

    String slug;

    String description;

    String cover;

    String template;

    Integer priority;

    String permalink;

    List<String> posts;

    public int getPostCount() {
        return posts == null ? 0 : posts.size();
    }
}
