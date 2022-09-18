package run.halo.app.theme.finders.vo;

import lombok.Builder;
import lombok.Value;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link Tag}.
 */
@Value
@Builder
public class TagVo {

    MetadataOperator metadata;

    Tag.TagSpec spec;

    Tag.TagStatus status;

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
            .metadata(tag.getMetadata())
            .spec(spec)
            .status(status)
            .build();
    }
}
