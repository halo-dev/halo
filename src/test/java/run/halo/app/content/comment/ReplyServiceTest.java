package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import run.halo.app.core.extension.content.Reply;
import run.halo.app.extension.Metadata;

/**
 * Tests for {@link ReplyService}.
 *
 * @author guqing
 * @since 2.0.0
 */
class ReplyServiceTest {
    private final Instant now = Instant.now();

    @Test
    void creationTimeAscComparator() {
        // creation time:
        // 1. now + 5s, name: 1
        // 2. now + 3s, name: 2
        // 3. now + 3s, name: 3
        // 5. now + 1s, name: 4
        // 6. now - 1s, name: 5
        // 7. null, name: 6
        Reply reply1 = createReply("1", now.plusSeconds(5));
        Reply reply2 = createReply("2", now.plusSeconds(3));
        Reply reply3 = createReply("3", now.plusSeconds(3));
        Reply reply4 = createReply("4", now.plusSeconds(1));
        Reply reply5 = createReply("5", now.minusSeconds(1));
        Reply reply6 = createReply("6", null);
        String result = Stream.of(reply1, reply2, reply3, reply4, reply5, reply6)
            .sorted(ReplyService.creationTimeAscComparator())
            .map(reply -> reply.getMetadata().getName())
            .collect(Collectors.joining(", "));
        assertThat(result).isEqualTo("5, 4, 2, 3, 1, 6");
    }

    Reply createReply(String name, Instant creationTime) {
        Reply reply = new Reply();
        reply.setMetadata(new Metadata());
        reply.getMetadata().setName(name);
        reply.getMetadata().setCreationTimestamp(now);
        reply.setSpec(new Reply.ReplySpec());
        reply.getSpec().setCreationTime(creationTime);
        return reply;
    }
}