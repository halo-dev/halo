package run.halo.app.theme.finders.vo;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Tag;

/**
 * A value object for {@link Tag}.
 */
@Value
@Builder
public class TagVo {

    String name;

    String displayName;

    String slug;

    String color;

    String cover;

    String permalink;

    List<String> posts;

    Map<String, String> annotations;

    /**
     * Convert {@link Tag} to {@link TagVo}.
     *
     * @param tag tag extension
     * @return tag value object
     */
    public static TagVo from(Tag tag) {
        Tag.TagSpec spec = tag.getSpec();
        Tag.TagStatus status = tag.getStatusOrDefault();
        return TagVo.builder()
            .name(tag.getMetadata().getName())
            .displayName(spec.getDisplayName())
            .slug(spec.getSlug())
            .color(spec.getColor())
            .cover(spec.getCover())
            .permalink(status.getPermalink())
            .posts(status.getPosts())
            .annotations(tag.getMetadata().getAnnotations())
            .build();
    }
}
