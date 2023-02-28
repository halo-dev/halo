package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.event.post.DownvotedEvent;
import run.halo.app.event.post.UpvotedEvent;
import run.halo.app.event.post.VisitedEvent;
import run.halo.app.extension.GroupVersion;

/**
 * Metrics counter endpoint.
 *
 * @author guqing
 * @since 2.0.0
 */
@AllArgsConstructor
@Component
public class TrackerEndpoint implements CustomEndpoint {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/Tracker";
        return SpringdocRouteBuilder.route()
            .POST("trackers/counter", this::increaseVisit,
                builder -> builder.operationId("count")
                    .description("Count an extension resource visits.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(CounterRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Void.class))
            )
            .POST("trackers/upvote", this::upvote,
                builder -> builder.operationId("upvote")
                    .description("Upvote an extension resource.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(VoteRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Void.class))
            )
            .POST("trackers/downvote", this::downvote,
                builder -> builder.operationId("downvote")
                    .description("Downvote an extension resource.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(VoteRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Void.class))
            )
            .build();
    }

    private Mono<ServerResponse> increaseVisit(ServerRequest request) {
        return request.bodyToMono(CounterRequest.class)
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Counter request body must not be empty")))
            .doOnNext(counterRequest -> {
                eventPublisher.publishEvent(new VisitedEvent(this, counterRequest.group(),
                    counterRequest.name(), counterRequest.plural()));
            })
            .then(ServerResponse.ok().build());
    }

    private Mono<ServerResponse> upvote(ServerRequest request) {
        return request.bodyToMono(VoteRequest.class)
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Upvote request body must not be empty")))
            .doOnNext(voteRequest -> {
                eventPublisher.publishEvent(new UpvotedEvent(this, voteRequest.group(),
                    voteRequest.name(), voteRequest.plural()));
            })
            .then(ServerResponse.ok().build());
    }

    private Mono<ServerResponse> downvote(ServerRequest request) {
        return request.bodyToMono(VoteRequest.class)
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Downvote request body must not be empty")))
            .doOnNext(voteRequest -> {
                eventPublisher.publishEvent(new DownvotedEvent(this, voteRequest.group(),
                    voteRequest.name(), voteRequest.plural()));
            })
            .then(ServerResponse.ok().build());
    }

    public record VoteRequest(String group, String plural, String name) {
    }

    public record CounterRequest(String group, String plural, String name, String hostname,
                                 String screen, String language, String referrer) {
        /**
         * Construct counter request.
         * group and session uid can be empty.
         */
        public CounterRequest {
            Assert.notNull(plural, "The plural must not be null.");
            Assert.notNull(name, "The name must not be null.");
            group = StringUtils.defaultString(group);
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.halo.run", "v1alpha1");
    }
}
