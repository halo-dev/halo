package run.halo.app.theme;

import lombok.Builder;
import lombok.Data;
import org.pf4j.ExtensionPoint;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;

/**
 * <p>{@link ReactivePostContentHandler} provides a way to extend the content to be displayed in
 * the theme.</p>
 * Plugins can implement this interface to extend the content to be displayed in the theme,
 * including but not limited to adding specific styles, JS libraries, inserting specific content,
 * and intercepting content.
 *
 * @author guqing
 * @since 2.7.0
 */
public interface ReactivePostContentHandler extends ExtensionPoint {

    /**
     * <p>Methods for handling {@link run.halo.app.core.extension.content.Post} content.</p>
     * <p>For example, you can use this method to change the content for a better display in
     * theme-side.</p>
     *
     * @param postContent content to be handled
     * @return handled content
     */
    Mono<PostContentContext> handle(@NonNull PostContentContext postContent);

    @Data
    @Builder
    class PostContentContext {
        private Post post;
        private String content;
        private String raw;
        private String rawType;
    }
}
