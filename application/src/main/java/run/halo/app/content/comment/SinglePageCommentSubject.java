package run.halo.app.content.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.infra.ExternalLinkProcessor;

/**
 * Comment subject for {@link SinglePage}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class SinglePageCommentSubject implements CommentSubject<SinglePage> {

    private final ReactiveExtensionClient client;

    private final ExternalLinkProcessor externalLinkProcessor;

    @Override
    public Mono<SinglePage> get(String name) {
        return client.fetch(SinglePage.class, name);
    }

    @Override
    public Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return get(name)
            .map(page -> {
                var url = externalLinkProcessor
                    .processLink(page.getStatusOrDefault().getPermalink());
                return new SubjectDisplay(page.getSpec().getTitle(), url, "页面");
            });
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        GroupVersionKind groupVersionKind =
            new GroupVersionKind(ref.getGroup(), ref.getVersion(), ref.getKind());
        return GroupVersionKind.fromExtension(SinglePage.class).equals(groupVersionKind);
    }
}
