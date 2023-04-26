package run.halo.app.theme.router.factories;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static run.halo.app.theme.finders.PostPublicQueryService.FIXED_PREDICATE;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.vo.PostVo;
import run.halo.app.theme.router.ViewNameResolver;

/**
 * The {@link PostRouteFactory} for generate {@link RouterFunction} specific to the template
 * <code>post.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class PostRouteFactory implements RouteFactory {

    private final PostFinder postFinder;

    private final ViewNameResolver viewNameResolver;

    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> create(String pattern) {
        PatternParser postParamPredicate =
            new PatternParser(pattern);
        if (postParamPredicate.isQueryParamPattern()) {
            RequestPredicate requestPredicate = postParamPredicate.toRequestPredicate();
            return RouterFunctions.route(GET("/")
                .and(requestPredicate), queryParamHandlerFunction(postParamPredicate));
        }
        return RouterFunctions
            .route(GET(pattern).and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    HandlerFunction<ServerResponse> queryParamHandlerFunction(PatternParser paramPredicate) {
        return request -> {
            Map<String, String> variables = mergedVariables(request);
            PostPatternVariable patternVariable = new PostPatternVariable();
            Optional.ofNullable(variables.get(paramPredicate.getQueryParamName()))
                .ifPresent(value -> {
                    switch (paramPredicate.getPlaceholderName()) {
                        case "name" -> patternVariable.setName(value);
                        case "slug" -> patternVariable.setSlug(value);
                        default ->
                            throw new IllegalArgumentException("Unsupported query param predicate");
                    }
                });
            return postResponse(request, patternVariable);
        };
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            PostPatternVariable patternVariable = PostPatternVariable.from(request);
            return postResponse(request, patternVariable);
        };
    }

    @NonNull
    private Mono<ServerResponse> postResponse(ServerRequest request,
        PostPatternVariable patternVariable) {
        Mono<PostVo> postVoMono = bestMatchPost(patternVariable);
        return postVoMono
            .flatMap(postVo -> {
                Map<String, Object> model = new HashMap<>();
                model.put("name", postVo.getMetadata().getName());
                model.put(ModelConst.TEMPLATE_ID, DefaultTemplateEnum.POST.getValue());
                model.put("groupVersionKind", GroupVersionKind.fromExtension(Post.class));
                GVK gvk = Post.class.getAnnotation(GVK.class);
                model.put("plural", gvk.plural());
                model.put("post", postVo);

                String template = postVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.POST.getValue())
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
            .next()
            .flatMap(post -> postFinder.getByName(post.getMetadata().getName()))
            .switchIfEmpty(Mono.error(new NotFoundException("Post not found")));
    }

    Flux<Post> postsByPredicates(PostPatternVariable patternVariable) {
        if (StringUtils.isNotBlank(patternVariable.getName())) {
            return fetchPostsByName(patternVariable.getName());
        }
        if (StringUtils.isNotBlank(patternVariable.getSlug())) {
            return fetchPostsBySlug(patternVariable.getSlug());
        }
        return Flux.empty();
    }

    private Flux<Post> fetchPostsByName(String name) {
        return client.fetch(Post.class, name)
            .filter(FIXED_PREDICATE)
            .flux();
    }

    private Flux<Post> fetchPostsBySlug(String slug) {
        return client.list(Post.class,
            post -> FIXED_PREDICATE.test(post)
                && matchIfPresent(slug, post.getSpec().getSlug()),
            null);
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

        static PostPatternVariable from(ServerRequest request) {
            Map<String, String> variables = mergedVariables(request);
            return JsonUtils.mapToObject(variables, PostPatternVariable.class);
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

    static class PatternParser {
        private static final Pattern PATTERN_COMPILE = Pattern.compile("([^&?]*)=\\{(.*?)\\}(&|$)");
        private static final Cache<String, Matcher> MATCHER_CACHE = CacheBuilder.newBuilder()
            .maximumSize(5)
            .build();

        private final String pattern;
        private String paramName;
        private String placeholderName;
        private final boolean isQueryParamPattern;

        PatternParser(String pattern) {
            this.pattern = pattern;
            Matcher matcher = patternToMatcher(pattern);
            if (matcher.find()) {
                this.paramName = matcher.group(1);
                this.placeholderName = matcher.group(2);
                this.isQueryParamPattern = true;
            } else {
                this.isQueryParamPattern = false;
            }
        }

        Matcher patternToMatcher(String pattern) {
            try {
                return MATCHER_CACHE.get(pattern, () -> PATTERN_COMPILE.matcher(pattern));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        RequestPredicate toRequestPredicate() {
            if (!this.isQueryParamPattern) {
                throw new IllegalStateException("Not a query param pattern: " + pattern);
            }

            return RequestPredicates.queryParam(paramName, value -> true);
        }

        public String getPlaceholderName() {
            return this.placeholderName;
        }

        public String getQueryParamName() {
            return this.paramName;
        }

        public boolean isQueryParamPattern() {
            return isQueryParamPattern;
        }
    }
}
