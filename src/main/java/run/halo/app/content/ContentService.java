package run.halo.app.content;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Snapshot;
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

    Mono<ContentWrapper> updateContent(ContentRequest content);

    Mono<ContentWrapper> publish(String headSnapshotName, Ref subjectRef);

    Mono<Snapshot> getBaseSnapshot(Ref subjectRef);

    Mono<Snapshot> latestSnapshotVersion(Ref subjectRef);

    Mono<Snapshot> latestPublishedSnapshot(Ref subjectRef);

    Flux<Snapshot> listSnapshots(Ref subjectRef);
}
