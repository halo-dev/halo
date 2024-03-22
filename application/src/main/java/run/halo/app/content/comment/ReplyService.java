package run.halo.app.content.comment;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.ListResult;

/**
 * An application service for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface ReplyService {

    Mono<Reply> create(String commentName, Reply reply);

    Mono<ListResult<ListedReply>> list(ReplyQuery query);

    Mono<Void> removeAllByComment(String commentName);
}
