package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static run.halo.app.extension.index.query.Queries.and;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;

import lombok.Data;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;

/**
 * Stats endpoint.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class StatsEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    public StatsEndpoint(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "SystemV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .GET("stats", this::getStats, builder -> builder.operationId("getStats")
                .description("Get stats.")
                .tag(tag)
                .response(responseBuilder()
                    .implementation(DashboardStats.class)
                )
            )
            .build();
    }

    Mono<ServerResponse> getStats(ServerRequest request) {
        var stats = DashboardStats.emptyStats();
        Mono<Void> setFromCounters = client.listAll(
                Counter.class, ListOptions.builder().build(), Sort.unsorted()
            )
            .doOnNext(counter -> {
                var visit = counter.getVisit();
                if (visit != null) {
                    stats.setVisits(stats.getVisits() + visit);
                }
                var totalComment = counter.getTotalComment();
                if (totalComment != null) {
                    stats.setComments(stats.getComments() + totalComment);
                }
                var approvedComment = counter.getApprovedComment();
                if (approvedComment != null) {
                    stats.setApprovedComments(
                        stats.getApprovedComments() + approvedComment
                    );
                }
                var upvote = counter.getUpvote();
                if (upvote != null) {
                    stats.setUpvotes(stats.getUpvotes() + upvote);
                }
            })
            .then();

        Mono<Void> setUsers = client.countBy(User.class, ListOptions.builder()
                .labelSelector()
                .notEq(User.HIDDEN_USER_LABEL, "true")
                .end()
                .andQuery(isNull("metadata.deletionTimestamp"))
                .build()
            )
            .doOnNext(stats::setUsers)
            .then();
        Mono<Void> setPosts = client.countBy(Post.class, ListOptions.builder()
                .andQuery(and(
                    isNull("metadata.deletionTimestamp"),
                    equal("spec.deleted", "false")
                ))
                .build()
            )
            .doOnNext(stats::setPosts)
            .then();

        return Mono.when(setFromCounters, setUsers, setPosts)
            .thenReturn(stats)
            .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    @Data
    public static class DashboardStats {
        private long visits;
        private long comments;
        private long approvedComments;
        private long upvotes;
        private long users;
        private long posts;

        /**
         * Creates an empty stats that populated initialize value.
         *
         * @return stats with initialize value.
         */
        public static DashboardStats emptyStats() {
            DashboardStats stats = new DashboardStats();
            stats.setVisits(0L);
            stats.setComments(0L);
            stats.setApprovedComments(0L);
            stats.setUpvotes(0L);
            stats.setUsers(0L);
            stats.setPosts(0L);
            return stats;
        }
    }
}
