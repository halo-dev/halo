package run.halo.app.content;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.SinglePage;
import run.halo.app.extension.ListResult;

/**
 * Single page service.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface SinglePageService {

    Mono<ListResult<ListedSinglePage>> list(SinglePageQuery listRequest);

    Mono<SinglePage> draft(SinglePageRequest pageRequest);

    Mono<SinglePage> update(SinglePageRequest pageRequest);

    Mono<SinglePage> publish(String name);
}
