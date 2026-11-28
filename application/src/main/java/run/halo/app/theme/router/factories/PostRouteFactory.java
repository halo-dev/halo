package run.halo.app.theme.router.factories;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.content.permalinks.PostPermalinkPolicy.DEFAULT_CATEGORY;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.i18n.LocaleContextResolver;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactivePresentationExtensionClient;
import run.halo.app.extension.index.query.Queries;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.JacksonUtils;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.ModelMapUtils;
import run.halo.app.theme.router.PostViewNameResolver;
import run.halo.app.theme.router.ReactiveQueryPostPredicateResolver;
import run.halo.app.theme.router.TitleVisibilityIdentifyCalculator;
import run.halo.app.theme.utils.PublicRoutePatterns;

/**
 * The {@link PostRouteFactory} for generate {@link RouterFunction} specific to the template <code>post.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class PostRouteFactory implements RouteFactory {

    private final PostFinder postFinder;

    private final PostViewNameResolver postViewNameResolver;

    private final ReactivePresentationExtensionClient client;

    private final ReactiveQueryPostPredicateResolver queryPostPredicateResolver;

    private final TitleVisibilityIdentifyCalculator titleVisibilityIdentifyCalculator;

    private final LocaleContextResolver localeContextResolver;
    private final PostService postService;
    private final ExternalUrlSupplier externalUrl;

    @Override
    public RouterFunction<ServerResponse> create(String pattern) {
        var postParamPredicate = new PatternParser(pattern);
        if (postParamPredicate.isQueryParamPattern()) {
            RequestPredicate requestPredicate = postParamPredicate.toRequestPredicate();
            return RouterFunctions.route(GET("/").and(requestPredicate), queryParamHandlerFunction(postParamPredicate));
        }
        return RouterFunctions.route(GET(pattern).and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    @Override
    public List<String> publicPatterns(String pattern) {
        return List.of();
    }

    @Override
    public Mono<Set<String>> claimedPrefixes(String pattern) {
        if (pattern == null || pattern.contains("?")) {
            return Mono.just(Set.of());
        }
        return publishedPostPaths()
                .map(PublicRoutePatterns::staticPrefix)
                .filter(prefix -> !prefix.isEmpty())
                .collect(Collectors.toSet())
                .map(Set::copyOf);
    }

    @Override
    public Mono<Boolean> matches(String pattern, String path, HttpMethod method) {
        if (!PublicRoutePatterns.supportsRead(method) || pattern == null || pattern.contains("?")) {
            return Mono.just(false);
        }
        var match = matchPath(pattern, path);
        if (match == null) {
            return Mono.just(false);
        }
        var name = match.getUriVariables().get("name");
        var slug = match.getUriVariables().get("slug");
        if (StringUtils.isBlank(name) && StringUtils.isBlank(slug)) {
            return publishedPostPaths().any(routePath -> PublicRoutePatterns.samePath(path, routePath));
        }

        var identity =
                StringUtils.isNotBlank(name) ? Queries.equal("metadata.name", name) : Queries.equal("spec.slug", slug);
        var options = ListOptions.builder()
                .andQuery(identity)
                .andQuery(Queries.isNull("metadata.deletionTimestamp"))
                .andQuery(Queries.equal("spec.deleted", false))
                .labelSelector()
                .eq(Post.PUBLISHED_LABEL, "true")
                .end()
                .build();
        return client.listAll(Post.class, options, Sort.unsorted())
                .filter(post -> post.getStatus() != null && post.getStatus().getPermalink() != null)
                .any(post -> PublicRoutePatterns.samePath(
                        path, permalinkPath(post.getStatus().getPermalink())));
    }

    private Flux<String> publishedPostPaths() {
        var options = ListOptions.builder()
                .andQuery(Queries.isNull("metadata.deletionTimestamp"))
                .andQuery(Queries.equal("spec.deleted", false))
                .labelSelector()
                .eq(Post.PUBLISHED_LABEL, "true")
                .end()
                .build();
        return client.listAll(Post.class, options, Sort.unsorted())
                .filter(post -> post.getStatus() != null && post.getStatus().getPermalink() != null)
                .map(post -> permalinkPath(post.getStatus().getPermalink()))
                .filter(path -> !path.isEmpty());
    }

    private PathPattern.PathMatchInfo matchPath(String pattern, String path) {
        try {
            return PathPatternParser.defaultInstance
                    .parse(pattern)
                    .matchAndExtract(PathContainer.parsePath(path.startsWith("/") ? path : "/" + path));
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private String permalinkPath(String value) {
        try {
            var uri = URI.create(value);
            var path = uri.getRawPath();
            if (path == null) {
                return "";
            }
            var base = basePath(externalUrl == null ? null : externalUrl.get());
            if (!base.isEmpty() && (path.equals(base) || path.startsWith(base + "/"))) {
                path = path.substring(base.length());
            }
            return path.isEmpty() ? "/" : path;
        } catch (IllegalArgumentException error) {
            return "";
        }
    }

    private static String basePath(URI uri) {
        if (uri == null || uri.getRawPath() == null) {
            return "";
        }
        return uri.getRawPath().replaceFirst("/$", "");
    }

    HandlerFunction<ServerResponse> queryParamHandlerFunction(PatternParser paramPredicate) {
        return request -> {
            Map<String, String> variables = mergedVariables(request);
            PostPatternVariable patternVariable = new PostPatternVariable();
            Optional.ofNullable(variables.get(paramPredicate.getParamName())).ifPresent(value -> {
                switch (paramPredicate.getPlaceholderName()) {
                    case "name" -> patternVariable.setName(value);
                    case "slug" -> patternVariable.setSlug(value);
                    default -> throw new IllegalArgumentException("Unsupported query param predicate");
                }
            });
            return postResponse(request, patternVariable);
        };
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            PostPatternVariable patternVariable = PostPatternVariable.from(request);
            return postResponse(request, patternVariable)
                    .switchIfEmpty(Mono.error(() -> new NotFoundException("Post not found.")));
        };
    }

    private Mono<ServerResponse> postResponse(ServerRequest request, PostPatternVariable patternVariable) {
        Mono<PostVo> postVoMono = bestMatchPost(patternVariable);
        return postVoMono
                .doOnNext(postVo -> {
                    postVo.getSpec()
                            .setTitle(titleVisibilityIdentifyCalculator.calculateTitle(
                                    postVo.getSpec().getTitle(),
                                    postVo.getSpec().getVisible(),
                                    localeContextResolver
                                            .resolveLocaleContext(request.exchange())
                                            .getLocale()));
                })
                .flatMap(postVo -> {
                    Map<String, Object> model = ModelMapUtils.postModel(postVo);
                    return postViewNameResolver
                            .resolveViewNameOrDefault(request, postVo)
                            .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
                });
    }

    Mono<PostVo> bestMatchPost(PostPatternVariable variable) {
        return postsByPredicates(variable)
                .filter(post -> {
                    Map<String, String> labels = MetadataUtil.nullSafeLabels(post);
                    return matchIfPresent(variable.getName(), post.getMetadata().getName())
                            && matchIfPresent(variable.getSlug(), post.getSpec().getSlug())
                            && matchIfPresent(variable.getYear(), labels.get(Post.ARCHIVE_YEAR_LABEL))
                            && matchIfPresent(variable.getMonth(), labels.get(Post.ARCHIVE_MONTH_LABEL))
                            && matchIfPresent(variable.getDay(), labels.get(Post.ARCHIVE_DAY_LABEL));
                })
                .filterWhen(post -> {
                    if (isNotBlank(variable.getCategorySlug())) {
                        var categoryNames = post.getSpec().getCategories();
                        return postService
                                .listCategories(categoryNames)
                                .next()
                                .filter(category -> category.getSpec().getSlug().equals(variable.getCategorySlug()))
                                .map(category -> category.getSpec().getSlug())
                                .switchIfEmpty(Mono.defer(() -> {
                                    if (DEFAULT_CATEGORY.equals(variable.getCategorySlug())) {
                                        return Mono.just(DEFAULT_CATEGORY);
                                    }
                                    return Mono.empty();
                                }))
                                .hasElement();
                    }
                    return Mono.just(true);
                })
                .next()
                .flatMap(post -> postFinder.getByName(post.getMetadata().getName()));
    }

    Flux<Post> postsByPredicates(PostPatternVariable patternVariable) {
        if (isNotBlank(patternVariable.getName())) {
            return fetchPostsByName(patternVariable.getName());
        }
        if (isNotBlank(patternVariable.getSlug())) {
            return fetchPostsBySlug(patternVariable.getSlug());
        }
        return Flux.empty();
    }

    private Flux<Post> fetchPostsByName(String name) {
        return queryPostPredicateResolver
                .getPredicate()
                .flatMap(predicate -> client.fetch(Post.class, name).filter(predicate))
                .flux();
    }

    private Flux<Post> fetchPostsBySlug(String slug) {
        return queryPostPredicateResolver.getListOptions().flatMapMany(listOptions -> {
            if (isNotBlank(slug)) {
                var other = Queries.equal("spec.slug", slug);
                listOptions.setFieldSelector(listOptions.getFieldSelector().andQuery(other));
            }
            return client.listAll(Post.class, listOptions, Sort.unsorted());
        });
    }

    private boolean matchIfPresent(String variable, String target) {
        return StringUtils.isBlank(variable) || StringUtils.equals(target, variable);
    }

    @Data
    static class PostPatternVariable {
        String name;
        String slug;
        String year;
        String month;
        String day;
        String categorySlug;

        static PostPatternVariable from(ServerRequest request) {
            Map<String, String> variables = mergedVariables(request);
            return JacksonUtils.mapToObject(variables, PostPatternVariable.class);
        }
    }

    static Map<String, String> mergedVariables(ServerRequest request) {
        Map<String, String> pathVariables = request.pathVariables();
        MultiValueMap<String, String> queryParams = request.queryParams();
        Map<String, String> mergedVariables = new LinkedHashMap<>();
        for (String paramKey : queryParams.keySet()) {
            mergedVariables.put(paramKey, queryParams.getFirst(paramKey));
        }
        // path variables higher priority will override query params
        mergedVariables.putAll(pathVariables);
        return mergedVariables;
    }

    @Getter
    static class PatternParser {
        private static final Pattern PATTERN_COMPILE = Pattern.compile("([^&?]*)=\\{(.*?)\\}(&|$)");

        private final String pattern;
        private String paramName;
        private String placeholderName;
        private final boolean isQueryParamPattern;

        PatternParser(String pattern) {
            this.pattern = pattern;
            var matcher = PATTERN_COMPILE.matcher(pattern);
            if (matcher.find()) {
                this.paramName = matcher.group(1);
                this.placeholderName = matcher.group(2);
                this.isQueryParamPattern = true;
            } else {
                this.isQueryParamPattern = false;
            }
        }

        RequestPredicate toRequestPredicate() {
            if (!this.isQueryParamPattern) {
                throw new IllegalStateException("Not a query param pattern: " + pattern);
            }

            return RequestPredicates.queryParam(paramName, value -> true);
        }
    }
}
