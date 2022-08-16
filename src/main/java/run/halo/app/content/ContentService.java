package run.halo.app.content;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Snapshot;

/**
 * @author guqing
 * @since 2.0.0
 */
public interface ContentService {

    Mono<ContentWrapper> getContent(String name);

    Mono<ContentWrapper> draftContent(ContentRequest content);

    Mono<ContentWrapper> updateContent(ContentRequest content);

    Mono<ContentWrapper> publish(String headSnapshotName, Snapshot.SubjectRef subjectRef);

    Mono<Snapshot> latestSnapshotVersion(Snapshot.SubjectRef subjectRef);

    Mono<Snapshot> latestPublishedSnapshot(Snapshot.SubjectRef subjectRef);
}
