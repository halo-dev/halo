package run.halo.app.extension.router;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Scheme;

class ExtensionListHandler implements ExtensionRouterFunctionFactory.ListHandler {
    private final Scheme scheme;

    private final ReactiveExtensionClient client;

    public ExtensionListHandler(Scheme scheme, ReactiveExtensionClient client) {
        this.scheme = scheme;
        this.client = client;
    }

    @Override
    @NonNull
    public Mono<ServerResponse> handle(@NonNull ServerRequest request) {
        var conversionService = ApplicationConversionService.getSharedInstance();
        var page =
            request.queryParam("page")
                .map(pageString -> conversionService.convert(pageString, Integer.class))
                .orElse(0);

        var size = request.queryParam("size")
            .map(sizeString -> conversionService.convert(sizeString, Integer.class))
            .orElse(0);

        var labelSelectors = request.queryParams().get("labelSelector");
        var fieldSelectors = request.queryParams().get("fieldSelector");

        // TODO Resolve comparator from request
        return client.list(scheme.type(),
                labelAndFieldSelectorToPredicate(labelSelectors, fieldSelectors), null, page, size)
            .flatMap(listResult -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(listResult));
    }

    @Override
    public String pathPattern() {
        return ExtensionRouterFunctionFactory.PathPatternGenerator.buildExtensionPathPattern(
            scheme);
    }
}
