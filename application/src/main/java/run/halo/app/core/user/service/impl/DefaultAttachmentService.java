package run.halo.app.core.user.service.impl;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferLimitException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.ThumbnailSize;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.core.extension.attachment.Policy;
import run.halo.app.core.extension.attachment.endpoint.AttachmentHandler;
import run.halo.app.core.extension.attachment.endpoint.DeleteOption;
import run.halo.app.core.extension.attachment.endpoint.SimpleFilePart;
import run.halo.app.core.extension.attachment.endpoint.UploadOption;
import run.halo.app.core.extension.service.AttachmentService;
import run.halo.app.extension.ConfigMap;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.HttpSecurityUtils;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;

@Component
class DefaultAttachmentService implements AttachmentService {

    private static final int MAX_RESPONSE_SIZE = 10 * 1024 * 1024; // 10 MB

    private final ReactiveExtensionClient client;

    private final ExtensionGetter extensionGetter;

    private WebClient webClient;

    public DefaultAttachmentService(ReactiveExtensionClient client, ExtensionGetter extensionGetter) {
        this.client = client;
        this.extensionGetter = extensionGetter;
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpSecurityUtils.secureHttpClient()))
                .filter(HttpSecurityUtils.maxResponseSizeFilter(MAX_RESPONSE_SIZE))
                .build();
    }

    /**
     * Only for testing purpose.
     *
     * @param webClient new web client
     */
    void setWebClient(WebClient webClient) {
        Assert.notNull(webClient, "WebClient must not be null");
        this.webClient = webClient;
    }

    @Override
    public Mono<Attachment> upload(
            String username,
            String policyName,
            @Nullable String groupName,
            FilePart filePart,
            @Nullable Consumer<Attachment> beforeCreating) {
        var builder = UploadOption.builder();
        builder.file(filePart);
        var getPolicyAndConfigMap = client.get(Policy.class, policyName)
                .doOnNext(builder::policy)
                .mapNotNull(p -> p.getSpec().getConfigMapName())
                .filter(StringUtils::hasText)
                .switchIfEmpty(Mono.error(
                        () -> new ServerWebInputException("ConfigMap name not found in Policy " + policyName)))
                .flatMap(configMapName -> client.get(ConfigMap.class, configMapName))
                .doOnNext(builder::configMap)
                .then();

        var getGroup = Mono.justOrEmpty(groupName)
                .filter(StringUtils::hasText)
                .flatMap(name -> client.get(Group.class, name))
                .doOnNext(builder::group)
                .then();
        return Mono.when(getPolicyAndConfigMap, getGroup)
                .then(Mono.fromSupplier(builder::build))
                .flatMap(uploadContext -> extensionGetter
                        .getExtensions(AttachmentHandler.class)
                        .concatMap(handler -> handler.upload(uploadContext))
                        .next())
                .switchIfEmpty(Mono.error(() ->
                        new ServerErrorException("No suitable handler found for uploading the attachment.", null)))
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
    public Mono<Attachment> upload(
            String policyName,
            @Nullable String groupName,
            String filename,
            Flux<DataBuffer> content,
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
                .flatMap(deleteOption -> extensionGetter
                        .getExtensions(AttachmentHandler.class)
                        .concatMap(handler -> handler.delete(deleteOption))
                        .next());
    }

    @Override
    public Mono<URI> getPermalink(Attachment attachment) {
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
                .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                        .flatMap(configMap -> extensionGetter
                                .getExtensions(AttachmentHandler.class)
                                .concatMap(handler -> handler.getPermalink(attachment, policy, configMap))
                                .next()));
    }

    @Override
    public Mono<URI> getSharedURL(Attachment attachment, Duration ttl) {
        return client.get(Policy.class, attachment.getSpec().getPolicyName())
                .flatMap(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName())
                        .flatMap(configMap -> extensionGetter
                                .getExtensions(AttachmentHandler.class)
                                .concatMap(handler -> handler.getSharedURL(attachment, policy, configMap, ttl))
                                .next()));
    }

    @Override
    public Mono<Map<ThumbnailSize, URI>> getThumbnailLinks(Attachment attachment) {

        return client.get(Policy.class, attachment.getSpec().getPolicyName())
                .zipWhen(policy -> client.get(ConfigMap.class, policy.getSpec().getConfigMapName()))
                .flatMap(tuple2 -> {
                    var policy = tuple2.getT1();
                    var configMap = tuple2.getT2();
                    return extensionGetter
                            .getExtensions(AttachmentHandler.class)
                            .concatMap(handler -> handler.getThumbnailLinks(attachment, policy, configMap))
                            .next();
                });
    }

    @Override
    public Mono<Attachment> uploadFromUrl(
            URL url, String policyName, @Nullable String groupName, @Nullable String filename) {
        return Mono.fromCallable(url::toURI)
                .flatMap(uri -> webClient
                        .get()
                        .uri(uri)
                        .accept(MediaType.APPLICATION_OCTET_STREAM)
                        .retrieve()
                        .onStatus(
                                HttpStatusCode::isError,
                                response -> Mono.error(
                                        new ServerWebInputException(MessageFormat.format("""
                            Failed to fetch the content from the external URL due to \
                            non-successful response status: {0}""", response.statusCode()))))
                        .toEntityFlux(DataBuffer.class)
                        .flatMap(response -> {
                            if (!response.hasBody() || Flux.empty().equals(response.getBody())) {
                                return Mono.error(new ServerWebInputException(
                                        "Failed to fetch the content from the external URL due to empty "
                                                + "response body."));
                            }
                            var body = response.getBody();
                            var headers = response.getHeaders();
                            var finalFilename = Optional.ofNullable(filename)
                                    .filter(StringUtils::hasText)
                                    .orElseGet(() -> getExternalUrlFilename(uri, headers));
                            var contentType = headers.getContentType();
                            return upload(policyName, groupName, finalFilename, requireNonNull(body), contentType);
                        })
                        .onErrorMap(WebClientRequestException.class, e -> {
                            if (e.getCause() instanceof UnknownHostException ex) {
                                return new ServerWebInputException("""
                            Unable to resolve host or private IP resolved: %s
                            """.formatted(ex.getMessage()));
                            }
                            return e;
                        })
                        .onErrorMap(WebClientResponseException.class, e -> {
                            if (e.getCause() instanceof DataBufferLimitException) {
                                return new ServerWebInputException("""
                            Response body from the external URL is too large to be buffered in \
                            memory. Please ensure the file size is within the allowed limit.""");
                            }
                            return e;
                        }));
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
                .switchIfEmpty(Mono.error(
                        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.")))
                .map(SecurityContext::getAuthentication)
                .flatMap(func);
    }
}
