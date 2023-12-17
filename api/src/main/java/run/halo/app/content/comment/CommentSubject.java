package run.halo.app.content.comment;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Ref;

/**
 * Comment subject.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentSubject<T extends Extension> extends ExtensionPoint {

    Mono<T> get(String name);

    default Mono<SubjectDisplay> getSubjectDisplay(String name) {
        return Mono.empty();
    }

    boolean supports(Ref ref);

    record SubjectDisplay(String title, String url, String kindName) {
    }
}
