package run.halo.app.core.extension.service.impl;

import java.net.URI;
import java.time.Duration;
import java.util.function.Function;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;
import run.halo.app.core.extension.attachment.endpoint.DeleteOption;
import run.halo.app.core.extension.attachment.endpoint.UploadOption;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.plugin.ExtensionComponentsFinder;

@Component
public class DefaultAttachmentService implements AttachmentService {

    private final ReactiveExtensionClient client;

    private final ExtensionComponentsFinder extensionComponentsFinder;

    public DefaultAttachmentService(ReactiveExtensionClient client,
        ExtensionComponentsFinder extensionComponentsFinder) {
        this.client = client;
        this.extensionComponentsFinder = extensionComponentsFinder;
    }

    @Override
    public Mono<Attachment> upload(@NonNull String policyName,
        @Nullable String groupName,
        @NonNull String filename,
        @NonNull Flux<DataBuffer> content,
        @Nullable MediaType mediaType) {
        return authenticationConsumer(authentication -> client.get(Policy.class, policyName)
            .flatMap(policy -> {
                var configMapName = policy.getSpec().getConfigMapName();
                if (!StringUtils.hasText(configMapName)) {
                    return Mono.error(new ServerWebInputException(
                        "ConfigMap name not found in Policy " + policyName));
                }
                return client.get(ConfigMap.class, configMapName)
                    .map(configMap -> UploadOption.from(filename,
                        content,
                        mediaType,
                        policy,
                        configMap));
            })
            .flatMap(uploadContext -> {
                var handlers = extensionComponentsFinder.getExtensions(AttachmentHandler.class);
                return Flux.fromIterable(handlers)
                    .concatMap(handler -> handler.upload(uploadContext))
                    .next();
            })
            .switchIfEmpty(Mono.error(() -> new ServerErrorException(
                "No suitable handler found for uploading the attachment.", null)))
            .doOnNext(attachment -> {
                var spec = attachment.getSpec();
                if (spec == null) {
                    spec = new Attachment.AttachmentSpec();
                    attachment.setSpec(spec);
                }
                spec.setOwnerName(authentication.getName());
                if (StringUtils.hasText(groupName)) {
                    spec.setGroupName(groupName);
                }
                spec.setPolicyName(policyName);
            })
            .flatMap(client::create));
    }

    @Override
    public Mono<Attachment> delete(Attachment attachment) {
        var spec = attachment.getSpec();
        return client.get(Policy.class, spec.getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .map(configMap -> new DeleteOption(attachment, policy, configMap)))
            .flatMap(deleteOption -> {
                var handlers = extensionComponentsFinder.getExtensions(AttachmentHandler.class);
                return Flux.fromIterable(handlers)
                    .concatMap(handler -> handler.delete(deleteOption))
                    .next();
            });
    }

    @Override
    public Mono<URI> getPermalink(Attachment attachment) {
        var handlers = extensionComponentsFinder.getExtensions(AttachmentHandler.class);
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .flatMap(configMap -> Flux.fromIterable(handlers)
                    .concatMap(handler -> handler.getPermalink(attachment, policy, configMap))
                    .next()));
    }

    @Override
    public Mono<URI> getSharedURL(Attachment attachment, Duration ttl) {
        var handlers = extensionComponentsFinder.getExtensions(AttachmentHandler.class);
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .flatMap(configMap -> Flux.fromIterable(handlers)
                    .concatMap(handler -> handler.getSharedURL(attachment, policy, configMap, ttl))
                    .next()));
    }

    private <T> Mono<T> authenticationConsumer(Function<Authentication, Mono<T>> func) {
        return ReactiveSecurityContextHolder.getContext()
            .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Authentication required.")))
            .map(SecurityContext::getAuthentication)
            .flatMap(func);
    }
}
