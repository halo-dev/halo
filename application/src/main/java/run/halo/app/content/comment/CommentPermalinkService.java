package run.halo.app.content.comment;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.net.URI;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.Ref;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

/** Resolves comment subjects and formats the shared frontend location fragment. */
@Component
@RequiredArgsConstructor
public class CommentPermalinkService {
    private final ExtensionGetter extensionGetter;

    @SuppressWarnings("unchecked")
    public Mono<String> getSubjectUrl(@Nullable Ref ref) {
        if (ref == null) {
            return Mono.empty();
        }
        return extensionGetter
                .getExtensions(CommentSubject.class)
                .filter(subject -> subject.supports(ref))
                .next()
                .flatMap(subject -> (Mono<CommentSubject.SubjectDisplay>) subject.getSubjectDisplay(ref.getName()))
                .mapNotNull(CommentSubject.SubjectDisplay::url);
    }

    public Mono<String> getPermalink(Comment comment) {
        return getSubjectUrl(comment.getSpec().getSubjectRef())
                .mapNotNull(url -> getPermalink(url, comment.getMetadata().getName(), null));
    }

    @Nullable
    public static String getPermalink(@Nullable String subjectUrl, String commentName, @Nullable String replyName) {
        if (StringUtils.isBlank(subjectUrl)) {
            return null;
        }
        try {
            var uri = URI.create(subjectUrl);
            var scheme = uri.getScheme();
            if (uri.isOpaque()
                    || (scheme != null && !"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                    || (scheme == null && StringUtils.isBlank(uri.getRawPath()))) {
                return null;
            }
            var fragment = "halo-comment=" + URLEncoder.encode(commentName, UTF_8);
            if (StringUtils.isNotBlank(replyName)) {
                fragment += "&reply=" + URLEncoder.encode(replyName, UTF_8);
            }
            return UriComponentsBuilder.fromUri(uri)
                    .fragment(fragment)
                    .build(true)
                    .toUriString();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
