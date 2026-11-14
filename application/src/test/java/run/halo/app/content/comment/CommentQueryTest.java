package run.halo.app.content.comment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;

/**
 * Tests for {@link CommentQuery}.
 *
 * @author kindaokay
 */
class CommentQueryTest {

    private CommentQuery createQuery(String rawQuery) {
        var request = MockServerHttpRequest.get("/apis/api.console.halo.run/v1alpha1/comments" + rawQuery)
                .build();
        var exchange = MockServerWebExchange.from(request);
        var serverRequest =
                ServerRequest.create(exchange, HandlerStrategies.withDefaults().messageReaders());
        return new CommentQuery(serverRequest);
    }

    @Test
    void shouldMatchPendingRepliesWhenFilteringByApprovedFalse() {
        var listOptions = createQuery("?approved=false").toListOptions();
        var fieldQuery = listOptions.getFieldSelector().query().toString();
        assertThat(fieldQuery).contains("spec.approved = false");
        assertThat(fieldQuery).contains("status.pendingReplyCount > 0");
    }

    @Test
    void shouldOnlyMatchApprovedCommentsWhenFilteringByApprovedTrue() {
        var listOptions = createQuery("?approved=true").toListOptions();
        var fieldQuery = listOptions.getFieldSelector().query().toString();
        assertThat(fieldQuery).contains("spec.approved = true");
        assertThat(fieldQuery).doesNotContain("pendingReplyCount");
    }

    @Test
    void shouldNotApplyApprovalConditionWithoutApprovedParam() {
        var listOptions = createQuery("").toListOptions();
        var fieldQuery = listOptions.getFieldSelector().query().toString();
        assertThat(fieldQuery).doesNotContain("spec.approved");
    }
}
