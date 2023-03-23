package run.halo.app.content.comment;

import java.time.Instant;
import java.util.Comparator;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;
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

    /**
     * Ascending order by creation time.
     *
     * @return reply comparator
     */
    static Comparator<Reply> creationTimeAscComparator() {
        Function<Reply, Instant> creationTime = reply -> reply.getSpec().getCreationTime();
        Function<Reply, Instant> metadataCreationTime =
            reply -> reply.getMetadata().getCreationTimestamp();
        // ascending order by creation time
        // asc nulls high will be placed at the end
        return Comparator.comparing(creationTime, Comparators.nullsHigh())
            .thenComparing(metadataCreationTime)
            .thenComparing(reply -> reply.getMetadata().getName());
    }
}
