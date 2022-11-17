package run.halo.app.theme.finders.vo;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * Post archives by month.
 *
 * @author guqing
 * @since 2.0.0
 */
@Value
@Builder
public class PostArchiveYearMonthVo {

    String month;

    List<ListedPostVo> posts;
}
