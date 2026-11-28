package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.extension.index.query.Queries.equal;
import static run.halo.app.extension.index.query.Queries.isNull;
import static run.halo.app.theme.router.PageUrlUtils.totalPage;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.i18n.LocaleContextResolver;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.user.service.RoleService;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.localization.ResourceReference;
import run.halo.app.security.authorization.AuthorityUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.ListedPostVo;
import run.halo.app.theme.finders.vo.UserVo;
import run.halo.app.theme.router.ModelConst;
import run.halo.app.theme.router.PageUrlUtils;
import run.halo.app.theme.router.TitleVisibilityIdentifyCalculator;
import run.halo.app.theme.router.UrlContextListResult;
import run.halo.app.theme.utils.PublicRoutePatterns;

/**
 * The {@link AuthorPostsRouteFactory} for generate {@link RouterFunction} specific to the template <code>index.html
 * </code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class AuthorPostsRouteFactory implements RouteFactory {

    private final PostFinder postFinder;
    private final ReactivePresentationExtensionClient client;
    private final RoleService roleService;
    private SystemConfigFetcher environmentFetcher;

    private final TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    private final LocaleContextResolver localeContextResolver;

    @Override
    public RouterFunction<ServerResponse> create(String pattern) {
        return RouterFunctions.route(
                GET("/authors/{name}")
                        .or(GET("/authors/{name}/page/{page:\\d+}"))
                        .and(accept(MediaType.TEXT_HTML)),
                handlerFunction());
    }

    @Override
    public List<String> publicPatterns(String pattern) {
        return List.of("/authors/{name}", "/authors/{name}/page/{page:\\d+}");
    }

    @Override
    public Mono<Boolean> matches(String pattern, String path, HttpMethod method) {
        if (!PublicRoutePatterns.supportsRead(method)) {
            return Mono.just(false);
        }
        var variables = PublicRoutePatterns.matchAndExtract(path, publicPatterns(pattern));
        if (variables.isEmpty()) {
            return Mono.just(false);
        }
        var name = variables.get().get("name");
        if (name == null || name.isBlank()) {
            return Mono.just(false);
        }
        return userExists(name).flatMap(exists -> exists ? hasPostManageRole(name) : Mono.just(false));
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            String name = request.pathVariable("name");
            return hasPostManageRole(name).flatMap(hasPostManageRole -> {
                if (hasPostManageRole) {
                    return ServerResponse.ok()
                            .render(
                                    DefaultTemplateEnum.AUTHOR.getValue(),
                                    Map.of(
                                            "name",
                                            name,
                                            "author",
                                            getByName(name),
                                            "posts",
                                            postList(request, name),
                                            ModelConst.TEMPLATE_ID,
                                            DefaultTemplateEnum.AUTHOR.getValue(),
                                            ModelConst.RESOURCE_REFERENCE,
                                            new ResourceReference(GroupVersionKind.fromExtension(User.class), name)));
                }
                return Mono.error(new NotFoundException("Author page not found."));
            });
        };
    }

    protected Mono<Boolean> hasPostManageRole(String username) {
        return roleService
                .getRolesByUsername(username)
                .collectList()
                .flatMap(roles -> roleService.contains(roles, Set.of(AuthorityUtils.POST_CONTRIBUTOR_ROLE_NAME)))
                .defaultIfEmpty(false);
    }

    private Mono<UrlContextListResult<ListedPostVo>> postList(ServerRequest request, String name) {
        String path = request.path();
        int pageNum = pageNumInPathVariable(request);
        return configuredPageSize(environmentFetcher, SystemSetting.Post::getAuthorPageSize)
                .flatMap(pageSize -> postFinder.listByOwner(pageNum, pageSize, name))
                .doOnNext(list -> {
                    list.getItems().forEach(listedPostVo -> {
                        listedPostVo
                                .getSpec()
                                .setTitle(titleVisibilityIdentifyCalculator.calculateTitle(
                                        listedPostVo.getSpec().getTitle(),
                                        listedPostVo.getSpec().getVisible(),
                                        localeContextResolver
                                                .resolveLocaleContext(request.exchange())
                                                .getLocale()));
                    });
                })
                .map(list -> new UrlContextListResult.Builder<ListedPostVo>()
                        .listResult(list)
                        .nextUrl(PageUrlUtils.nextPageUrl(path, totalPage(list)))
                        .prevUrl(PageUrlUtils.prevPageUrl(path))
                        .build());
    }

    private Mono<UserVo> getByName(String name) {
        return client.fetch(User.class, name)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Author page not found.")))
                .map(UserVo::from);
    }

    private Mono<Boolean> userExists(String name) {
        var listOptions = ListOptions.builder()
                .andQuery(equal("metadata.name", name))
                .andQuery(isNull("metadata.deletionTimestamp"))
                .build();
        return client.countBy(User.class, listOptions).map(count -> count > 0);
    }
}
