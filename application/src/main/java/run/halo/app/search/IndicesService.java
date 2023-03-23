package run.halo.app.search;

import reactor.core.publisher.Mono;

public interface IndicesService {

    Mono<Void> rebuildPostIndices();

}
