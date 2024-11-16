package run.halo.app.core.user.service.impl;

import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
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
import run.halo.app.core.extension.attachment.endpoint.SimpleFilePart;
import run.halo.app.core.extension.attachment.endpoint.UploadOption;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveUrlDataBufferFetcher;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Component
public class DefaultAttachmentService implements AttachmentService {

    private final ReactiveExtensionClient client;

    private final ExtensionGetter extensionGetter;

    private final ReactiveUrlDataBufferFetcher dataBufferFetcher;

    public DefaultAttachmentService(ReactiveExtensionClient client,
        ExtensionGetter extensionGetter,
        ReactiveUrlDataBufferFetcher dataBufferFetcher) {
        this.client = client;
        this.extensionGetter = extensionGetter;
        this.dataBufferFetcher = dataBufferFetcher;
    }

    @Override
    public Mono<Attachment> upload(
        @NonNull String username,
        @NonNull String policyName,
        @Nullable String groupName,
        @NonNull FilePart filePart,
        @Nullable Consumer<Attachment> beforeCreating) {
        return client.get(Policy.class, policyName)
            .flatMap(policy -> {
                var configMapName = policy.getSpec().getConfigMapName();
                if (!StringUtils.hasText(configMapName)) {
                    return Mono.error(new ServerWebInputException(
                        "ConfigMap name not found in Policy " + policyName));
                }
                return client.get(ConfigMap.class, configMapName)
                    .map(configMap -> new UploadOption(filePart, policy, configMap));
            })
            .flatMap(uploadContext -> extensionGetter.getExtensions(AttachmentHandler.class)
                .concatMap(handler -> handler.upload(uploadContext))
                .next())
            .switchIfEmpty(Mono.error(() -> new ServerErrorException(
                "No suitable handler found for uploading the attachment.", null)))
            .doOnNext(attachment -> {
                var spec = attachment.getSpec();
                if (spec == null) {
                    spec = new Attachment.AttachmentSpec();
                    attachment.setSpec(spec);
                }
                spec.setOwnerName(username);
                if (StringUtils.hasText(groupName)) {
                    spec.setGroupName(groupName);
                }
                spec.setPolicyName(policyName);
            })
            .doOnNext(attachment -> {
                if (beforeCreating != null) {
                    beforeCreating.accept(attachment);
                }
            })
            .flatMap(client::create);
    }

    @Override
    public Mono<Attachment> upload(@NonNull String policyName,
        @Nullable String groupName,
        @NonNull String filename,
        @NonNull Flux<DataBuffer> content,
        @Nullable MediaType mediaType) {
        var file = new SimpleFilePart(filename, content, mediaType);
        return authenticationConsumer(
            authentication -> upload(authentication.getName(), policyName, groupName, file, null));
    }

    @Override
    public Mono<Attachment> delete(Attachment attachment) {
        var spec = attachment.getSpec();
        return client.get(Policy.class, spec.getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .map(configMap -> new DeleteOption(attachment, policy, configMap)))
            .flatMap(deleteOption -> extensionGetter.getExtensions(AttachmentHandler.class)
                .concatMap(handler -> handler.delete(deleteOption))
                .next());
    }

    @Override
    public Mono<URI> getPermalink(Attachment attachment) {
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .flatMap(configMap -> extensionGetter.getExtensions(AttachmentHandler.class)
                    .concatMap(handler -> handler.getPermalink(attachment, policy, configMap))
                    .next()
                )
            );
    }

    @Override
    public Mono<URI> getSharedURL(Attachment attachment, Duration ttl) {
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
            .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                .flatMap(configMap -> extensionGetter.getExtensions(AttachmentHandler.class)
                    .concatMap(handler -> handler.getSharedURL(attachment, policy, configMap, ttl))
                    .next()
                )
            );
    }

    @Override
    public Mono<Attachment> uploadFromUrl(@NonNull URL url, @NonNull String policyName,
        String groupName, String filename) {
        var uri = URI.create(url.toString());
        AtomicReference<MediaType> mediaTypeRef = new AtomicReference<>();
        AtomicReference<String> fileNameRef = new AtomicReference<>(filename);

        Mono<Flux<DataBuffer>> contentMono = dataBufferFetcher.head(uri)
            .map(response -> {
                var httpHeaders = response.getHeaders();
                if (!StringUtils.hasText(fileNameRef.get())) {
                    fileNameRef.set(getExternalUrlFilename(uri, httpHeaders));
                }
                MediaType contentType = httpHeaders.getContentType();
                mediaTypeRef.set(contentType);
                return response;
            })
            .map(response -> dataBufferFetcher.fetch(uri));

        return contentMono.flatMap(
                (content) -> upload(policyName, groupName, fileNameRef.get(), content,
                    mediaTypeRef.get())
            )
            .onErrorResume(throwable -> Mono.error(
                new ServerWebInputException(
                    "Failed to transfer the attachment from the external URL."))
            );
    }

    private static String getExternalUrlFilename(URI externalUrl, HttpHeaders httpHeaders) {
        String fileName = httpHeaders.getContentDisposition().getFilename();
        if (!StringUtils.hasText(fileName)) {
            var path = externalUrl.getPath();
            fileName = Paths.get(path).getFileName().toString();
        }
        // TODO get file extension from media type
        return fileName;
    }

    private <T> Mono<T> authenticationConsumer(Function<Authentication, Mono<T>> func) {
        return ReactiveSecurityContextHolder.getContext()
            .switchIfEmpty(Mono.error(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Authentication required.")))
            .map(SecurityContext::getAuthentication)
            .flatMap(func);
    }
}
