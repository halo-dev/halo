package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.Assert;
import run.halo.app.core.extension.content.SinglePage;

/**
 * A value object for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class SinglePageVo extends ListedSinglePageVo {

    private ContentVo content;

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
