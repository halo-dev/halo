package run.halo.app.theme.router.poc;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.util.UriUtils.encodePath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Watcher;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.ViewNameResolver;
import run.halo.app.theme.router.strategy.ModelConst;

/**
 * The {@link SinglePageRoute} for route request to specific template <code>page.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class SinglePageRoute implements RouterFunction<ServerResponse>,
    InitializingBean {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);

    private final Map<NameSlugPair, HandlerFunction<ServerResponse>> quickRouteMap =
        new ConcurrentHashMap<>();

    private final ReactiveExtensionClient client;

    private final SinglePageFinder singlePageFinder;

    private final ViewNameResolver viewNameResolver;

    @Override
    @NonNull
    public Mono<HandlerFunction<ServerResponse>> route(@NonNull ServerRequest request) {
        return Flux.fromIterable(routerFunctions())
            .concatMap(routerFunction -> routerFunction.route(request))
            .next();
    }

    @Override
    public void accept(@NonNull RouterFunctions.Visitor visitor) {
        routerFunctions().forEach(routerFunction -> routerFunction.accept(visitor));
    }

    private List<RouterFunction<ServerResponse>> routerFunctions() {
        return quickRouteMap.keySet().stream()
            .map(nameSlugPair -> {
                String routePath = singlePageRoute(nameSlugPair.slug());
                return RouterFunctions.route(GET(routePath)
                        .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    handlerFunction(nameSlugPair.name()));
            })
            .collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        client.watch(new SinglePageWatcher());
    }

    record NameSlugPair(String name, String slug) {
    }

    String singlePageRoute(String slug) {
        var permalink = encodePath(slug, UTF_8);
        return StringUtils.prependIfMissing(permalink, "/");
    }

    private void quickRouteMapUpdater(SinglePageOperation singlePageOperation) {
        Operation operation = singlePageOperation.operation();
        NameSlugPair nameSlug = singlePageOperation.nameSlug();
        switch (operation) {
            case ADD, UPDATE -> quickRouteMap.put(nameSlug, handlerFunction(nameSlug.name()));
            case DELETE -> quickRouteMap.remove(nameSlug);
            default -> {
            }
        }
    }

    HandlerFunction<ServerResponse> handlerFunction(String name) {
        return request -> singlePageFinder.getByName(name)
            .flatMap(singlePageVo -> {
                Map<String, Object> model = new HashMap<>();
                model.put("groupVersionKind", gvk);
                model.put("plural", getPlural());
                model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.SINGLE_PAGE.getValue());
                model.put("singlePage", singlePageVo);
                String template = singlePageVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.SINGLE_PAGE.getValue())
                    .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
            });
    }

    record SinglePageOperation(NameSlugPair nameSlug, Operation operation) {
    }

    enum Operation {
        ADD, UPDATE, DELETE
    }

    private String getPlural() {
        GVK annotation = SinglePage.class.getAnnotation(GVK.class);
        return annotation.plural();
    }

    class SinglePageWatcher implements Watcher {
        private boolean disposed = false;
        private Runnable disposeHook;

        @Override
        public void onAdd(Extension extension) {
            if (isNotSinglePage(extension)) {
                return;
            }
            SinglePageOperation op =
                createSingleOperation((SinglePage) extension, Operation.ADD);
            quickRouteMapUpdater(op);
        }

        private SinglePageOperation createSingleOperation(SinglePage page, Operation operation) {
            var pair = new NameSlugPair(page.getMetadata().getName(), page.getSpec().getSlug());
            return new SinglePageOperation(pair, operation);
        }

        @Override
        public void onUpdate(Extension oldExtension, Extension newExtension) {
            if (isNotSinglePage(newExtension)) {
                return;
            }
            SinglePageOperation op =
                createSingleOperation((SinglePage) newExtension, Operation.UPDATE);
            quickRouteMapUpdater(op);
        }

        @Override
        public void onDelete(Extension extension) {
            if (isNotSinglePage(extension)) {
                return;
            }
            SinglePageOperation op =
                createSingleOperation((SinglePage) extension, Operation.DELETE);
            quickRouteMapUpdater(op);
        }

        @Override
        public void registerDisposeHook(Runnable dispose) {
            this.disposeHook = dispose;
        }

        @Override
        public void dispose() {
            if (isDisposed()) {
                return;
            }
            this.disposed = true;
            if (this.disposeHook != null) {
                this.disposeHook.run();
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        private boolean isNotSinglePage(Extension extension) {
            return !(extension instanceof SinglePage);
        }
    }
}
