package run.halo.app.content.comment;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Comment;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Ref;

/**
 * An application service for {@link Comment}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface CommentService {

    Mono<ListResult<ListedComment>> listComment(CommentQuery query);

    Mono<Comment> create(Comment comment);

    Mono<Void> removeBySubject(@NonNull Ref subjectRef);
}
