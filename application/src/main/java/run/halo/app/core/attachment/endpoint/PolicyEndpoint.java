package run.halo.app.core.attachment.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import java.util.HashMap;
import java.util.Objects;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
class PolicyEndpoint implements CustomEndpoint {

    private final ReactiveExtensionClient client;

    private final ObjectMapper mapper;

    private final ReactiveTransactionManager txManager;

    PolicyEndpoint(ReactiveExtensionClient client, ObjectMapper mapper,
        ReactiveTransactionManager txManager) {
        this.client = client;
        this.mapper = mapper;
        this.txManager = txManager;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "PolicyAlpha1Console";
        return SpringdocRouteBuilder.route()
            .GET(
                "/policies/{name}/configs/{group}",
                this::getPolicyConfigByGroup,
                builder -> builder.operationId("getPolicyConfigByGroup")
                    .description("Get policy config by group")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of the policy")
                        .required(true)
                        .implementation(String.class)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .description("Name of the group")
                        .required(true)
                        .implementation(String.class)
                    )
                    .response(responseBuilder().implementation(JsonNode.class))
            )
            .PUT(
                "/policies/{name}/configs/{group}",
                RequestPredicates.contentType(MediaType.APPLICATION_JSON),
                this::updatePolicyConfigByGroup,
                builder -> builder.operationId("updatePolicyConfigByGroup")
                    .description("Update policy config by group")
                    .tag(tag)
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("name")
                        .description("Name of the policy")
                        .required(true)
                        .implementation(String.class)
                    )
                    .parameter(parameterBuilder()
                        .in(ParameterIn.PATH)
                        .name("group")
                        .description("Name of the group")
                        .required(true)
                        .implementation(String.class)
                    )
                    .requestBody(
                        requestBodyBuilder().required(true).implementation(JsonNode.class))
                    .response(
                        responseBuilder().responseCode(String.valueOf(NO_CONTENT.value()))
                    )
            )
            .build();
    }

    private Mono<ServerResponse> updatePolicyConfigByGroup(ServerRequest serverRequest) {
        var policyName = serverRequest.pathVariable("name");
        var configGroup = serverRequest.pathVariable("group");
        return serverRequest.bodyToMono(JsonNode.class)
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "Request body is required.")
            ))
            .flatMap(jsonNode -> {
                var tx = TransactionalOperator.create(txManager);
                return client.get(Policy.class, policyName)
                    .flatMap(policy -> Mono.justOrEmpty(policy.getSpec())
                        .mapNotNull(Policy.PolicySpec::getConfigMapName)
                        .filter(StringUtils::hasText)
                        .flatMap(cmName -> client.fetch(ConfigMap.class, cmName))
                        .switchIfEmpty(Mono.fromSupplier(() -> {
                            // create a new configmap
                            var cm = new ConfigMap();
                            cm.setMetadata(new Metadata());
                            cm.getMetadata().setGenerateName(policyName + "-config-");
                            return cm;
                        }))
                        .flatMap(cm -> Mono.fromCallable(() -> {
                            if (cm.getData() == null) {
                                cm.setData(new HashMap<>());
                            }
                            var oldJson = cm.getData().get(configGroup);
                            if (StringUtils.hasText(oldJson)
                                && Objects.equals(jsonNode, mapper.readTree(oldJson))) {
                                // skip if no change
                                return null;
                            }
                            var newJson = mapper.writeValueAsString(jsonNode);
                            cm.getData().put(configGroup, newJson);
                            return cm;
                        }))
                        .flatMap(cm -> {
                            if (cm.getMetadata().getVersion() != null) {
                                return client.update(cm);
                            }
                            return client.create(cm);
                        })
                        .flatMap(cm -> {
                            var cmName = cm.getMetadata().getName();
                            if (policy.getSpec() != null
                                && Objects.equals(policy.getSpec().getConfigMapName(), cmName)) {
                                return Mono.just(cm);
                            }
                            if (policy.getSpec() == null) {
                                policy.setSpec(new Policy.PolicySpec());
                            }
                            policy.getSpec().setConfigMapName(cmName);
                            return client.update(policy);
                        })
                    )
                    .as(tx::transactional);
            })
            .then(ServerResponse.noContent().build());
    }

    private Mono<ServerResponse> getPolicyConfigByGroup(ServerRequest serverRequest) {
        var policyName = serverRequest.pathVariable("name");
        var configGroup = serverRequest.pathVariable("group");

        return client.get(Policy.class, policyName)
            .filter(p -> p.getSpec() != null)
            .map(p -> p.getSpec().getConfigMapName())
            .filter(StringUtils::hasText)
            .flatMap(cmName -> client.fetch(ConfigMap.class, cmName))
            .filter(cm -> cm.getData() != null && cm.getData().containsKey(configGroup))
            .map(cm -> cm.getData().get(configGroup))
            .flatMap(json -> Mono.fromCallable(() -> mapper.readTree(json)))
            .defaultIfEmpty(mapper.nullNode())
            .flatMap(config -> ServerResponse.ok().bodyValue(config));
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.storage.halo.run/v1alpha1");
    }
}
