package run.halo.app.theme.router.strategy;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.MediaType;
import org.springframework.http.server.PathContainer;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.core.extension.Post;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexer;
import run.halo.app.theme.router.TemplateRouterStrategy;

/**
 * The {@link PostRouterStrategy} for generate {@link RouterFunction} specific to the template
 * <code>post.html</code>.
 *
 * @author guqing
 * @since 2.0.0
 */
public class PostRouterStrategy implements TemplateRouterStrategy {
    private final PermalinkIndexer permalinkIndexer;

    public PostRouterStrategy(PermalinkIndexer permalinkIndexer) {
        this.permalinkIndexer = permalinkIndexer;
    }

    @Override
    public RouterFunction<ServerResponse> getRouteFunction(String template, String pattern) {
        PostRequestParamPredicate postParamPredicate =
            new PostRequestParamPredicate(pattern);

        if (postParamPredicate.isQueryParamPattern()) {
            String paramName = postParamPredicate.getParamName();
            String placeholderName = postParamPredicate.getPlaceholderName();
            RequestPredicate requestPredicate = postParamPredicate.requestPredicate();
            return RouterFunctions.route(requestPredicate,
                request -> {
                    String name = null;
                    if (PostRequestParamPredicate.NAME_PARAM.equals(
                        placeholderName)) {
                        name = request.queryParam(paramName).orElseThrow();
                    }

                    if (PostRequestParamPredicate.SLUG_PARAM.equals(
                        placeholderName)) {
                        name = permalinkIndexer.getSlugByName(
                            PostRequestParamPredicate.GVK,
                            placeholderName);
                    }
                    if (name == null) {
                        throw new NotFoundException("Post not found.");
                    }
                    return ServerResponse.ok()
                        .render(DefaultTemplateEnum.POST.getValue(),
                            Map.of(PostRequestParamPredicate.NAME_PARAM, name)
                        );
                });
        }
        return RouterFunctions
            .route(GET(pattern).and(accept(MediaType.TEXT_HTML)),
                request -> {
                    List<String> permalinks =
                        permalinkIndexer.getPermalinks(
                            PostRequestParamPredicate.GVK);
                    boolean contains = permalinks.contains(request.path());
                    if (!contains) {
                        throw new NotFoundException("Post not found.");
                    }
                    ExtensionLocator extensionLocator =
                        permalinkIndexer.lookup(request.path());
                    PathPattern parse = PathPatternParser.defaultInstance.parse(pattern);
                    PathPattern.PathMatchInfo pathMatchInfo =
                        parse.matchAndExtract(PathContainer.parsePath(request.path()));
                    Map<String, String> uriVariables = new HashMap<>();
                    uriVariables.put(PostRequestParamPredicate.NAME_PARAM,
                        extensionLocator.name());
                    if (pathMatchInfo != null) {
                        uriVariables.putAll(pathMatchInfo.getUriVariables());
                    }
                    return ServerResponse.ok()
                        .render(DefaultTemplateEnum.POST.getValue(), uriVariables);
                });
    }

    class PostRequestParamPredicate {
        static final String NAME_PARAM = "name";
        static final String SLUG_PARAM = "slug";
        static final GroupVersionKind GVK = GroupVersionKind.fromExtension(Post.class);
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
                return RequestPredicates.queryParam(paramName, name -> {
                    List<String> names = permalinkIndexer.getNames(GVK);
                    return names.contains(name);
                });
            }

            if (SLUG_PARAM.equals(placeholderName)) {
                return RequestPredicates.queryParam(paramName, slug -> {
                    List<String> slugs = permalinkIndexer.getSlugs(GVK);
                    return slugs.contains(slug);
                });
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

        public String getParamName() {
            return this.paramName;
        }

        public String getPlaceholderName() {
            return placeholderName;
        }

        public boolean isQueryParamPattern() {
            return isQueryParamPattern;
        }
    }
}
