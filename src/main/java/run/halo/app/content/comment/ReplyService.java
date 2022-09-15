package run.halo.app.content.comment;

import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Reply;

/**
 * An application service for {@link Reply}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface ReplyService {

    Mono<Reply> create(String commentName, Reply reply);
}
