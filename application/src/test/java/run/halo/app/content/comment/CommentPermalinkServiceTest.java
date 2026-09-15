package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Ref;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

class CommentPermalinkServiceTest {
    @Test
    void preservesRawUrlAndEncodesTargetNames() {
        assertThat(CommentPermalinkService.getPermalink(
                        "https://example.com/halo/archives/a%20b?language=zh&filter=a%26b#old",
                        "comment &=+%", "回复 /#"))
                .isEqualTo("https://example.com/halo/archives/a%20b?language=zh&filter=a%26b"
                        + "#halo-comment=comment+%26%3D%2B%25&reply=%E5%9B%9E%E5%A4%8D+%2F%23");
        assertThat(CommentPermalinkService.getPermalink("/archives/example", "comment-a", null))
                .isEqualTo("/archives/example#halo-comment=comment-a");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {" ", "/invalid path", "javascript:alert(1)", "#existing-fragment", "mailto:user@example.com"})
    void omitsUnusableSubjectUrls(String url) {
        assertThat(CommentPermalinkService.getPermalink(url, "comment-a", null)).isNull();
    }

    @Test
    void resolvesCurrentSubjectUrlAndPreservesMissingProviders() {
        var extensions = mock(ExtensionGetter.class);
        var service = new CommentPermalinkService(extensions);
        var provider = mock(PostCommentSubject.class);
        var ref = Ref.of("post-a", Post.GVK);
        when(extensions.getExtensions(CommentSubject.class)).thenReturn(Flux.empty());
        service.getSubjectUrl(ref).as(StepVerifier::create).verifyComplete();

        when(extensions.getExtensions(CommentSubject.class)).thenReturn(Flux.just(provider));
        when(provider.supports(ref)).thenReturn(true);
        when(provider.getSubjectDisplay("post-a")).thenReturn(Mono.empty());
        service.getSubjectUrl(ref).as(StepVerifier::create).verifyComplete();

        when(provider.getSubjectDisplay("post-a"))
                .thenReturn(Mono.just(new CommentSubject.SubjectDisplay("Post", "https://example.com/old", "Post")))
                .thenReturn(Mono.just(new CommentSubject.SubjectDisplay("Post", "https://example.com/new", "Post")))
                .thenReturn(Mono.error(new IllegalStateException("storage failure")));
        service.getSubjectUrl(ref)
                .as(StepVerifier::create)
                .expectNext("https://example.com/old")
                .verifyComplete();
        service.getSubjectUrl(ref)
                .as(StepVerifier::create)
                .expectNext("https://example.com/new")
                .verifyComplete();
        service.getSubjectUrl(ref).as(StepVerifier::create).verifyErrorMessage("storage failure");
    }
}
