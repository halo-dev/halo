package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.SinglePage;

/**
 * A value object for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SinglePageVo extends BasePostVo {

    /**
     * Convert {@link SinglePage} to {@link SinglePageVo}.
     *
     * @param singlePage single page extension
     * @return special page value object
     */
    public static SinglePageVo from(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        SinglePage.SinglePageSpec spec = singlePage.getSpec();
        SinglePage.SinglePageStatus pageStatus = singlePage.getStatus();
        return SinglePageVo.builder()
            .name(singlePage.getMetadata().getName())
            .annotations(singlePage.getMetadata().getAnnotations())
            .title(spec.getTitle())
            .cover(spec.getCover())
            .allowComment(spec.getAllowComment())
            .owner(spec.getOwner())
            .pinned(spec.getPinned())
            .slug(spec.getSlug())
            .htmlMetas(nullSafe(spec.getHtmlMetas()))
            .published(spec.getPublished())
            .publishTime(spec.getPublishTime())
            .priority(spec.getPriority())
            .version(spec.getVersion())
            .visible(spec.getVisible())
            .template(spec.getTemplate())
            .permalink(pageStatus.getPermalink())
            .excerpt(pageStatus.getExcerpt())
            .contributors(List.of())
            .build();
    }
}
