package run.halo.app.core.endpoint.console;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.plugin.UiPluginBundleService;

@Component
public class UiPluginEndpoint implements CustomEndpoint, InitializingBean {

    private final UiPluginBundleService uiPluginBundleService;

    private final WebProperties webProperties;

    private boolean useLastModified;

    private CacheControl bundleCacheControl = CacheControl.empty();

    public UiPluginEndpoint(UiPluginBundleService uiPluginBundleService, WebProperties webProperties) {
        this.uiPluginBundleService = uiPluginBundleService;
        this.webProperties = webProperties;
    }

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "UiPluginV1alpha1Console";
        return SpringdocRouteBuilder.route()
                .GET(
                        "ui-plugins/-/bundle.js",
                        this::fetchJsBundle,
                        builder -> builder.operationId("fetchUiPluginJsBundle")
                                .description("Merge JS bundles of enabled plugins and the activated theme into one.")
                                .tag(tag)
                                .response(responseBuilder().implementation(String.class)))
                .GET(
                        "ui-plugins/-/bundle.css",
                        this::fetchCssBundle,
                        builder -> builder.operationId("fetchUiPluginCssBundle")
                                .description("Merge CSS bundles of enabled plugins and the activated theme into one.")
                                .tag(tag)
                                .response(responseBuilder().implementation(String.class)))
                .build();
    }

    @Override
    public void afterPropertiesSet() {
        var cache = this.webProperties.getResources().getCache();
        this.useLastModified = cache.isUseLastModified();
        var cacheControl = cache.getCachecontrol().toHttpCacheControl();
        if (cacheControl != null) {
            this.bundleCacheControl = cacheControl;
        }
    }

    private Mono<ServerResponse> fetchJsBundle(ServerRequest request) {
        return fetchBundle(
                request, "js", "bundle.js", MediaType.valueOf("text/javascript"), uiPluginBundleService::getJsBundle);
    }

    private Mono<ServerResponse> fetchCssBundle(ServerRequest request) {
        return fetchBundle(
                request, "css", "bundle.css", MediaType.valueOf("text/css"), uiPluginBundleService::getCssBundle);
    }

    private Mono<ServerResponse> fetchBundle(
            ServerRequest request,
            String type,
            String filename,
            MediaType mediaType,
            java.util.function.Function<String, Mono<Resource>> bundleGetter) {
        return request.queryParam("v")
                .map(version -> bundleGetter
                        .apply(version)
                        .flatMap(resource -> bundleResponse(request, resource, filename, mediaType)))
                .orElseGet(() -> uiPluginBundleService
                        .generateBundleVersion()
                        .flatMap(version -> ServerResponse.temporaryRedirect(buildBundleUri(type, version))
                                .cacheControl(CacheControl.noStore())
                                .build()));
    }

    private Mono<ServerResponse> bundleResponse(
            ServerRequest request, Resource resource, String filename, MediaType mediaType) {
        var bodyBuilder = ServerResponse.ok().cacheControl(bundleCacheControl).contentType(mediaType);
        if (useLastModified) {
            try {
                var lastModified = Instant.ofEpochMilli(resource.lastModified());
                bodyBuilder = bodyBuilder.lastModified(lastModified);
            } catch (FileNotFoundException e) {
                return Mono.error(new NoResourceFoundException(request.uri(), filename));
            } catch (IOException e) {
                return Mono.error(e);
            }
        }
        return bodyBuilder.body(BodyInserters.fromResource(resource));
    }

    URI buildBundleUri(String type, String version) {
        return URI.create("/apis/api.console.halo.run/v1alpha1/ui-plugins/-/bundle." + type + "?v=" + version);
    }
}
