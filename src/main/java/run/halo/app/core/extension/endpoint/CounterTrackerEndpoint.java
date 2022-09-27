package run.halo.app.core.extension.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.content.Builder.contentBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.fn.builders.schema.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersion;
import run.halo.app.metrics.MeterUtils;

/**
 * Metrics counter endpoint.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CounterTrackerEndpoint implements CustomEndpoint {

    private final MeterRegistry meterRegistry;

    public CounterTrackerEndpoint(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.halo.run/v1alpha1/CounterTracker";
        return SpringdocRouteBuilder.route()
            .POST("countertrackers", this::increase,
                builder -> builder.operationId("Counter")
                    .description("Counter an extension resource visits.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .content(contentBuilder()
                            .mediaType(MediaType.APPLICATION_JSON_VALUE)
                            .schema(Builder.schemaBuilder()
                                .implementation(CounterRequest.class))
                        ))
                    .response(responseBuilder()
                        .implementation(Double.class))
            )
            .build();
    }

    private Mono<ServerResponse> increase(ServerRequest request) {
        return request.bodyToMono(CounterRequest.class)
            .switchIfEmpty(
                Mono.error(new IllegalArgumentException("Counter request body must not be empty")))
            .map(counterRequest -> {
                String counterName =
                    MeterUtils.nameOf(counterRequest.group(), counterRequest.plural(),
                        counterRequest.name());

                Counter counter = MeterUtils.visitCounter(meterRegistry, counterName);
                counter.increment();
                return (int) counter.count();
            })
            .flatMap(count -> ServerResponse.ok().bodyValue(count));
    }

    public record CounterRequest(String group, String plural, String name, String sessionUid) {
        /**
         * Construct counter request.
         * group and session uid can be empty.
         */
        public CounterRequest {
            Assert.notNull(plural, "The plural must not be null.");
            Assert.notNull(name, "The name must not be null.");
            sessionUid = StringUtils.defaultString(group);
            group = StringUtils.defaultString(group);
        }
    }

    @Override
    public GroupVersion groupVersion() {
        return new GroupVersion("api.halo.run", "v1alpha1");
    }
}
