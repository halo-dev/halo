package run.halo.app.theme.finders;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.theme.finders.vo.ListedSinglePageVo;
import run.halo.app.theme.finders.vo.SinglePageVo;

/**
 * A service that converts {@link SinglePage} to {@link SinglePageVo}.
 *
 * @author guqing
 * @since 2.6.0
 */
public interface SinglePageConversionService {

    Mono<SinglePageVo> convertToVo(SinglePage singlePage, String snapshotName);

    Mono<SinglePageVo> convertToVo(SinglePage singlePage);

    Mono<ListedSinglePageVo> convertToListedVo(SinglePage singlePage);
}
