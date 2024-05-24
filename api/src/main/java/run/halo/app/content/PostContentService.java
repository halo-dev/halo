package run.halo.app.content;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostContentService {

    Mono<ContentWrapper> getHeadContent(String postName);

    Mono<ContentWrapper> getReleaseContent(String postName);

    Mono<ContentWrapper> getSpecifiedContent(String postName, String snapshotName);

    Flux<String> listSnapshots(String postName);
}
