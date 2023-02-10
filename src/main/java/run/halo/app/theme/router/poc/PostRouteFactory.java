package run.halo.app.theme.router.poc;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
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
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.GVK;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostFinder;
import run.halo.app.theme.finders.impl.PostFinderImpl;
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
        PostRequestParamPredicate postParamPredicate =
            new PostRequestParamPredicate(pattern);
        if (postParamPredicate.isQueryParamPattern()) {
            RequestPredicate requestPredicate = postParamPredicate.requestPredicate();
            return RouterFunctions.route(requestPredicate, handlerFunction());
        }
        return RouterFunctions
            .route(GET(pattern).and(accept(MediaType.TEXT_HTML)), handlerFunction());
    }

    HandlerFunction<ServerResponse> handlerFunction() {
        return request -> {
            PostPatternVariable patternVariable = PostPatternVariable.from(request);
            Mono<PostVo> postVoMono = bestMatchPost(patternVariable);
            return postVoMono
                .flatMap(postVo -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("groupVersionKind", GroupVersionKind.fromExtension(Post.class));
                    GVK gvk = Post.class.getAnnotation(GVK.class);
                    model.put("plural", gvk.plural());
                    model.put("post", postVo);

                    String template = postVo.getSpec().getTemplate();
                    return viewNameResolver.resolveViewNameOrDefault(request, template,
                            DefaultTemplateEnum.POST.getValue())
                        .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
                })
                .switchIfEmpty(ServerResponse.notFound().build());
        };
    }

    Mono<PostVo> bestMatchPost(PostPatternVariable variable) {
        return postsByPredicates(variable)
            .filter(post -> {
                Map<String, String> labels = ExtensionUtil.nullSafeLabels(post);
                String year = labels.get(Post.ARCHIVE_YEAR_LABEL);
                String month = labels.get(Post.ARCHIVE_MONTH_LABEL);
                String day = labels.get(Post.ARCHIVE_DAY_LABEL);
                return matchIfPresent(variable.getName(), post.getMetadata().getName())
                    && matchIfPresent(variable.getSlug(), post.getSpec().getSlug())
                    && matchIfPresent(variable.getYear(), year)
                    && matchIfPresent(variable.getMonth(), month)
                    && matchIfPresent(variable.getDay(), day);
            })
            .next()
            .flatMap(post -> postFinder.getByName(post.getMetadata().getName()));
    }

    Flux<Post> postsByPredicates(PostPatternVariable patternVariable) {
        // fetch post by name
        if (StringUtils.isNotBlank(patternVariable.getName())) {
            return client.fetch(Post.class, patternVariable.getName())
                .filter(PostFinderImpl.FIXED_PREDICATE)
                .flux();
        }

        return client.list(Post.class,
            post -> PostFinderImpl.FIXED_PREDICATE.test(post)
                && matchIfPresent(patternVariable.getSlug(), post.getSpec().getSlug()),
            null);
    }

    private boolean matchIfPresent(String variable, String target) {
        if (StringUtils.isBlank(variable)) {
            return true;
        }
        return variable.equals(target);
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
    }

    class PostRequestParamPredicate {
        static final String NAME_PARAM = "name";
        static final String SLUG_PARAM = "slug";
        private final String pattern;
        private String paramName;
        private String placeholderName;

        private final boolean isQueryParamPattern;

        PostRequestParamPredicate(String pattern) {
            this.pattern = pattern;
            Matcher matcher = matchUrlParam(pattern);
            if (matcher != null) {
                this.paramName = matcher.group(1);
                this.placeholderName = matcher.group(2);
                this.isQueryParamPattern = true;
            } else {
                this.isQueryParamPattern = false;
            }
        }

        RequestPredicate requestPredicate() {
            if (!this.isQueryParamPattern) {
                throw new IllegalStateException("Not a query param pattern: " + pattern);
            }

            if (NAME_PARAM.equals(placeholderName)) {
                return RequestPredicates.queryParam(paramName,
                    name -> client.fetch(Post.class, name).blockOptional().isPresent());
            }

            if (SLUG_PARAM.equals(placeholderName)) {
                return RequestPredicates.queryParam(paramName,
                    slug -> client.list(Post.class, post -> post.getSpec().getSlug().equals(slug),
                        null).next().blockOptional().isPresent());
            }
            throw new IllegalArgumentException(
                String.format("Unknown param value placeholder [%s] in pattern [%s]",
                    placeholderName, pattern));
        }

        Matcher matchUrlParam(String patternSequence) {
            Pattern compile = Pattern.compile("([^&?]*)=\\{(.*?)\\}(&|$)");
            Matcher matcher = compile.matcher(patternSequence);
            if (matcher.find()) {
                return matcher;
            }
            return null;
        }

        public boolean isQueryParamPattern() {
            return isQueryParamPattern;
        }
    }
}
