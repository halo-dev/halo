package run.halo.app.content;

import static run.halo.app.content.HtmlThumbnailSrcsetInjector.generateSrcset;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailService;
import run.halo.app.theme.ReactivePostContentHandler;

/**
 * A post content handler to handle post html content and generate thumbnail by the img tag.
 *
 * @author guqing
 * @since 2.19.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostContentThumbnailHandler implements ReactivePostContentHandler {
    private final ThumbnailService thumbnailService;

    @Override
    public Mono<PostContentContext> handle(@NonNull PostContentContext postContent) {
        var html = postContent.getContent();
        return HtmlThumbnailSrcsetInjector.injectSrcset(html,
                src -> generateSrcset(URI.create(src), thumbnailService)
            )
            .onErrorResume(throwable -> {
                log.debug("Failed to inject srcset to post content, fallback to original content",
                    throwable);
                return Mono.just(html);
            })
            .doOnNext(postContent::setContent)
            .thenReturn(postContent);
    }
}
