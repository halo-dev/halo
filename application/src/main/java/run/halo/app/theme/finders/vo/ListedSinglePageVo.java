package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.MetadataOperator;

/**
 * A value object for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@ToString
@EqualsAndHashCode
public class ListedSinglePageVo implements ExtensionVoOperator {

    private MetadataOperator metadata;

    private SinglePage.SinglePageSpec spec;

    private SinglePage.SinglePageStatus status;

    private StatsVo stats;

    private List<ContributorVo> contributors;

    private ContributorVo owner;

    /**
     * Convert {@link SinglePage} to {@link ListedSinglePageVo}.
     *
     * @param singlePage single page extension
     * @return special page value object
     */
    public static ListedSinglePageVo from(SinglePage singlePage) {
        Assert.notNull(singlePage, "The singlePage must not be null.");
        SinglePage.SinglePageSpec spec = singlePage.getSpec();
        SinglePage.SinglePageStatus pageStatus = singlePage.getStatus();
        return ListedSinglePageVo.builder()
            .metadata(singlePage.getMetadata())
            .spec(spec)
            .status(pageStatus)
            .contributors(List.of())
            .build();
    }
}
