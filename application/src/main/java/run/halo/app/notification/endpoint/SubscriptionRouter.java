package run.halo.app.notification.endpoint;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ExternalUrlSupplier;

/**
 * A router for {@link Subscription}.
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class SubscriptionRouter {

    public static final String UNSUBSCRIBE_PATTERN =
        "/apis/api.notification.halo.run/v1alpha1/subscriptions/{name}/unsubscribe";

    private final ExternalUrlSupplier externalUrlSupplier;
    private final ReactiveExtensionClient client;

    @Bean
    RouterFunction<ServerResponse> notificationSubscriptionRouter() {
        return SpringdocRouteBuilder.route()
            .GET(UNSUBSCRIBE_PATTERN, this::unsubscribe, builder -> {
                builder.operationId("Unsubscribe")
                    .tag("api.notification.halo.run/v1alpha1/Subscription")
                    .description("Unsubscribe a subscription")
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Subscription name")
                        .required(true)
                    ).parameter(parameterBuilder()
                        .in(ParameterIn.QUERY)
                        .name("token")
                        .description("Unsubscribe token")
                        .required(true)
                    )
                    .response(responseBuilder().implementation(String.class))
                    .build();
            })
            .build();
    }

    Mono<ServerResponse> unsubscribe(ServerRequest request) {
        var name = request.pathVariable("name");
        var token = request.queryParam("token").orElse("");
        return client.fetch(Subscription.class, name)
            .filter(subscription -> {
                var unsubscribeToken = subscription.getSpec().getUnsubscribeToken();
                return StringUtils.equals(token, unsubscribeToken);
            })
            .flatMap(client::delete)
            .then(Mono.defer(() -> ServerResponse.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("Unsubscribe successfully."))
            );
    }

    /**
     * Gets unsubscribe url from the given subscription.
     *
     * @param subscription subscription must not be null
     * @return unsubscribe url
     */
    public String getUnsubscribeUrl(Subscription subscription) {
        var name = subscription.getMetadata().getName();
        var token = subscription.getSpec().getUnsubscribeToken();
        var externalUrl = defaultIfNull(externalUrlSupplier.getRaw(), URI.create("/"));
        return UriComponentsBuilder.fromUriString(externalUrl.toString())
            .path(UNSUBSCRIBE_PATTERN)
            .queryParam("token", token)
            .build(name)
            .toString();
    }
}
