package run.halo.app.content.comment;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;

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

    @Override
    public Mono<SinglePage> get(String name) {
        return client.fetch(SinglePage.class, name);
    }

    @Override
    public Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return get(name)
                .map(page -> new SubjectDisplay(
                        page.getSpec().getTitle(), page.getStatusOrDefault().getPermalink(), "页面"));
    }

    @Override
    public boolean supports(Ref ref) {
        Assert.notNull(ref, "Subject ref must not be null.");
        var gvk = SinglePage.GVK;
        return Objects.equals(gvk.group(), ref.getGroup()) && Objects.equals(gvk.kind(), ref.getKind());
    }
}
