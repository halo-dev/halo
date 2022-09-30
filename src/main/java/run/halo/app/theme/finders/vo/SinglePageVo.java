package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.util.Assert;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@Builder
@ToString
@EqualsAndHashCode
public class SinglePageVo {

    private MetadataOperator metadata;

    private SinglePage.SinglePageSpec spec;

    private SinglePage.SinglePageStatus status;

    private ContentVo content;

    private StatsVo stats;

    private List<Contributor> contributors;

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
            .metadata(singlePage.getMetadata())
            .spec(spec)
            .status(pageStatus)
            .contributors(List.of())
            .content(new ContentVo(null, null))
            .build();
    }
}
