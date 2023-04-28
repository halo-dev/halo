package run.halo.app.theme.router;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
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
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionOperator;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.SinglePageFinder;
import run.halo.app.theme.router.factories.ModelConst;

/**
 * The {@link SinglePageRoute} for route request to specific template <code>page.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
public class SinglePageRoute
    implements RouterFunction<ServerResponse>, Reconciler<Reconciler.Request>, DisposableBean {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(SinglePage.class);

    private final Map<NameSlugPair, HandlerFunction<ServerResponse>> quickRouteMap =
        new ConcurrentHashMap<>();

    private final ExtensionClient client;

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
    public Result reconcile(Request request) {
        client.fetch(SinglePage.class, request.name())
            .ifPresent(page -> {
                if (ExtensionOperator.isDeleted(page)
                    || BooleanUtils.isTrue(page.getSpec().getDeleted())) {
                    quickRouteMap.remove(NameSlugPair.from(page));
                    return;
                }
                // put new one
                quickRouteMap.entrySet()
                    .removeIf(entry -> entry.getKey().name().equals(request.name()));
                quickRouteMap.put(NameSlugPair.from(page), handlerFunction(request.name()));
            });
        return new Result(false, null);
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new SinglePage())
            .build();
    }

    @Override
    public void destroy() throws Exception {
        quickRouteMap.clear();
    }

    record NameSlugPair(String name, String slug) {
        public static NameSlugPair from(SinglePage page) {
            return new NameSlugPair(page.getMetadata().getName(), page.getSpec().getSlug());
        }
    }

    String singlePageRoute(String slug) {
        return StringUtils.prependIfMissing(slug, "/");
    }

    HandlerFunction<ServerResponse> handlerFunction(String name) {
        return request -> singlePageFinder.getByName(name)
            .flatMap(singlePageVo -> {
                Map<String, Object> model = new HashMap<>();
                model.put("name", singlePageVo.getMetadata().getName());
                model.put("groupVersionKind", gvk);
                model.put("plural", getPlural());
                model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.SINGLE_PAGE.getValue());
                model.put("singlePage", singlePageVo);
                String template = singlePageVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.SINGLE_PAGE.getValue())
                    .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
            })
            .switchIfEmpty(
                Mono.error(new NotFoundException("Single page not found"))
            );
    }

    private String getPlural() {
        GVK gvk = Scheme.getGvkFromType(SinglePage.class);
        return gvk.plural();
    }
}
