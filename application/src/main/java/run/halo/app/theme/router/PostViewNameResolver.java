package run.halo.app.theme.router;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.ViewNameResolver;
import run.halo.app.theme.finders.vo.PostVo;

/** Resolves the view name used to render post detail pages. */
@Component
@RequiredArgsConstructor
public class PostViewNameResolver {

    private final ViewNameResolver viewNameResolver;

    public Mono<String> resolveViewNameOrDefault(ServerRequest request, PostVo postVo) {
        return resolvePostTemplate(request, postVo)
                .switchIfEmpty(Mono.defer(() -> resolveCategoryPostTemplate(request, postVo)))
                .switchIfEmpty(Mono.defer(() ->
                        viewNameResolver.resolveViewNameOrDefault(request, null, DefaultTemplateEnum.POST.getValue())));
    }

    private Mono<String> resolvePostTemplate(ServerRequest request, PostVo postVo) {
        String template = postVo.getSpec().getTemplate();
        if (isBlank(template)) {
            return Mono.empty();
        }
        return viewNameResolver.resolveViewNameOrDefault(request, template, null);
    }

    private Mono<String> resolveCategoryPostTemplate(ServerRequest request, PostVo postVo) {
        return Flux.fromIterable(defaultIfNull(postVo.getCategories(), List.of()))
                .filter(category -> isNotBlank(category.getSpec().getPostTemplate()))
                .concatMap(category -> viewNameResolver.resolveViewNameOrDefault(
                        request, category.getSpec().getPostTemplate(), null))
                .next();
    }
}
