package run.halo.app.content;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Snapshot;
import run.halo.app.extension.Ref;

/**
 * Content service for {@link Snapshot}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface ContentService {

    Mono<ContentWrapper> getContent(String name);

    Mono<ContentWrapper> draftContent(ContentRequest content);

    Mono<ContentWrapper> draftContent(ContentRequest content, String parentName);

    Mono<ContentWrapper> updateContent(ContentRequest content);

    Mono<Snapshot> getBaseSnapshot(Ref subjectRef);

    Mono<Snapshot> latestSnapshotVersion(Ref subjectRef);

    Flux<Snapshot> listSnapshots(Ref subjectRef);
}
