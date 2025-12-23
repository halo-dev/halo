package run.halo.app.core.endpoint.console;

import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.endpoint.AttachmentHandler;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;

@Slf4j
@Component
@RequiredArgsConstructor
class AttachmentConsoleEndpoint implements CustomEndpoint {

    private final SystemConfigFetcher systemConfigFetcher;

    private final AttachmentHandler attachmentHandler;

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.storage.halo.run/v1alpha1");
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "AttachmentV1alpha1Console";
        return SpringdocRouteBuilder.route()
            .POST(
                path("/attachments/-/upload")
                    .and(contentType(MediaType.MULTIPART_FORM_DATA)),
                this::handleUpload,
                builder -> {
                    builder.operationId("uploadAttachmentForConsole")
                        .tag(tag)
                        .description("Upload attachment endpoint for console.");
                    this.attachmentHandler.buildDoc(builder);
                }
            )
            .build();
    }

    private Mono<ServerResponse> handleUpload(ServerRequest serverRequest) {
        var getConfig = systemConfigFetcher.fetch(
                SystemSetting.Attachment.GROUP,
                SystemSetting.Attachment.class
            )
            .mapNotNull(SystemSetting.Attachment::console)
            .filter(ac -> StringUtils.hasText(ac.policyName()))
            .switchIfEmpty(Mono.error(() -> new ServerWebInputException(
                "Attachment system setting is not configured for console"
            )));
        return attachmentHandler.handleUpload(serverRequest, getConfig);
    }

}
