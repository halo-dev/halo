package run.halo.app.content.comment;

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

    Mono<Comment> updateContent(String name, CommentContentRequest request);

    Mono<Void> removeBySubject(Ref subjectRef);
}
