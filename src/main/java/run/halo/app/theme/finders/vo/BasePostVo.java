package run.halo.app.theme.finders.vo;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import run.halo.app.core.extension.Post;

/**
 * a base entity for post.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@ToString
@SuperBuilder
public abstract class BasePostVo {
    String name;

    String title;

    String slug;

    String owner;

    String template;

    String cover;

    Boolean published;

    Instant publishTime;

    Boolean pinned;

    Boolean allowComment;

    Post.VisibleEnum visible;

    Integer version;

    Integer priority;

    String excerpt;

    List<Map<String, String>> htmlMetas;

    String permalink;

    List<Contributor> contributors;

    Map<String, String> annotations;

    static <T> List<T> nullSafe(List<T> t) {
        return Objects.requireNonNullElse(t, List.of());
    }
}
