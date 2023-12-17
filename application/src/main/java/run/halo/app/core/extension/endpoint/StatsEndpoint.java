package run.halo.app.core.extension.endpoint;

import static java.lang.Boolean.parseBoolean;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import lombok.Data;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Counter;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.MetadataUtil;
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
        final var tag = "api.console.halo.run/v1alpha1/Stats";
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
        return client.list(Counter.class, null, null)
            .reduce(DashboardStats.emptyStats(), (stats, counter) -> {
                stats.setVisits(stats.getVisits() + counter.getVisit());
                stats.setComments(stats.getComments() + counter.getTotalComment());
                stats.setApprovedComments(
                    stats.getApprovedComments() + counter.getApprovedComment());
                stats.setUpvotes(stats.getUpvotes() + counter.getUpvote());
                return stats;
            })
            .flatMap(stats -> client.list(User.class,
                    user -> {
                        var labels = MetadataUtil.nullSafeLabels(user);
                        return user.getMetadata().getDeletionTimestamp() == null
                            && !parseBoolean(labels.getOrDefault(User.HIDDEN_USER_LABEL, "false"));
                    },
                    null)
                .count()
                .map(count -> {
                    stats.setUsers(count.intValue());
                    return stats;
                }))
            .flatMap(stats -> client.list(Post.class, post -> !post.isDeleted(), null)
                .count()
                .map(count -> {
                    stats.setPosts(count.intValue());
                    return stats;
                })
            )
            .flatMap(stats -> ServerResponse.ok().bodyValue(stats));
    }

    @Data
    public static class DashboardStats {
        private Integer visits;
        private Integer comments;
        private Integer approvedComments;
        private Integer upvotes;
        private Integer users;
        private Integer posts;

        /**
         * Creates an empty stats that populated initialize value.
         *
         * @return stats with initialize value.
         */
        public static DashboardStats emptyStats() {
            DashboardStats stats = new DashboardStats();
            stats.setVisits(0);
            stats.setComments(0);
            stats.setApprovedComments(0);
            stats.setUpvotes(0);
            stats.setUsers(0);
            stats.setPosts(0);
            return stats;
        }
    }
}
