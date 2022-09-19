package run.halo.app.content.comment;

import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Ref;

/**
 * Comment subject.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentSubject<T extends AbstractExtension>  extends ExtensionPoint {

    Mono<T> get(String name);

    boolean supports(Ref ref);
}
