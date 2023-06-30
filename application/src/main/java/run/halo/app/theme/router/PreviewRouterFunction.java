package run.halo.app.theme.router;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.content.PostService;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.AnonymousUserConst;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.finders.PostPublicQueryService;
import run.halo.app.theme.finders.SinglePageConversionService;
import run.halo.app.theme.finders.vo.ContributorVo;
import run.halo.app.theme.finders.vo.PostVo;

/**
 * <p>Preview router for previewing posts and single pages.</p>
 *
 * @author guqing
 * @since 2.6.0
 */
@Component
@RequiredArgsConstructor
public class PreviewRouterFunction {
    static final String SNAPSHOT_NAME_PARAM = "snapshotName";

    private final ReactiveExtensionClient client;

    private final PostPublicQueryService postPublicQueryService;

    private final ViewNameResolver viewNameResolver;

    private final PostService postService;

    private final SinglePageConversionService singlePageConversionService;

    @Bean
    RouterFunction<ServerResponse> previewRouter() {
        return RouterFunctions.route()
            .GET("/preview/posts/{name}", this::previewPost)
            .GET("/preview/singlepages/{name}", this::previewSinglePage)
            .build();
    }

    private Mono<ServerResponse> previewPost(ServerRequest request) {
        final var name = request.pathVariable("name");
        return currentAuthenticatedUserName()
            .flatMap(principal -> client.fetch(Post.class, name))
            .flatMap(post -> {
                String snapshotName = request.queryParam(SNAPSHOT_NAME_PARAM)
                    .orElse(post.getSpec().getHeadSnapshot());
                return convertToPostVo(post, snapshotName);
            })
            .flatMap(post -> canPreview(post.getContributors())
                .doOnNext(canPreview -> {
                    if (!canPreview) {
                        throw new NotFoundException("Post not found.");
                    }
                })
                .thenReturn(post)
            )
            // Check permissions before throwing this exception
            .switchIfEmpty(Mono.error(() -> new NotFoundException("Post not found.")))
            .flatMap(postVo -> {
                String template = postVo.getSpec().getTemplate();
                Map<String, Object> model = ModelMapUtils.postModel(postVo);
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.POST.getValue())
                    .flatMap(templateName -> ServerResponse.ok().render(templateName, model));
            });
    }

    private Mono<PostVo> convertToPostVo(Post post, String snapshotName) {
        return postPublicQueryService.convertToVo(post, snapshotName)
            .doOnNext(postVo -> {
                // fake some attributes only for preview when they are not published
                Post.PostSpec spec = postVo.getSpec();
                if (spec.getPublishTime() == null) {
                    spec.setPublishTime(Instant.now());
                }
                if (spec.getPublish() == null) {
                    spec.setPublish(false);
                }
                Post.PostStatus status = postVo.getStatus();
                if (status == null) {
                    status = new Post.PostStatus();
                    postVo.setStatus(status);
                }
                if (status.getLastModifyTime() == null) {
                    status.setLastModifyTime(Instant.now());
                }
            });
    }

    private Mono<ServerResponse> previewSinglePage(ServerRequest request) {
        final var name = request.pathVariable("name");
        return currentAuthenticatedUserName()
            .flatMap(principal -> client.fetch(SinglePage.class, name))
            .flatMap(singlePage -> {
                String snapshotName = request.queryParam(SNAPSHOT_NAME_PARAM)
                    .orElse(singlePage.getSpec().getHeadSnapshot());
                return singlePageConversionService.convertToVo(singlePage, snapshotName);
            })
            .doOnNext(pageVo -> {
                // fake some attributes only for preview when they are not published
                SinglePage.SinglePageSpec spec = pageVo.getSpec();
                if (spec.getPublishTime() == null) {
                    spec.setPublishTime(Instant.now());
                }
                if (spec.getPublish() == null) {
                    spec.setPublish(false);
                }
                SinglePage.SinglePageStatus status = pageVo.getStatus();
                if (status == null) {
                    status = new SinglePage.SinglePageStatus();
                    pageVo.setStatus(status);
                }
                if (status.getLastModifyTime() == null) {
                    status.setLastModifyTime(Instant.now());
                }
            })
            .flatMap(singlePageVo -> canPreview(singlePageVo.getContributors())
                .doOnNext(canPreview -> {
                    if (!canPreview) {
                        throw new NotFoundException("Single page not found.");
                    }
                })
                .thenReturn(singlePageVo)
            )
            // Check permissions before throwing this exception
            .switchIfEmpty(Mono.error(() -> new NotFoundException("Single page not found.")))
            .flatMap(singlePageVo -> {
                Map<String, Object> model = ModelMapUtils.singlePageModel(singlePageVo);
                String template = singlePageVo.getSpec().getTemplate();
                return viewNameResolver.resolveViewNameOrDefault(request, template,
                        DefaultTemplateEnum.SINGLE_PAGE.getValue())
                    .flatMap(viewName -> ServerResponse.ok().render(viewName, model));
            });
    }

    private Mono<Boolean> canPreview(List<ContributorVo> contributors) {
        Assert.notNull(contributors, "The contributors must not be null");
        Set<String> contributorNames = contributors.stream()
            .map(ContributorVo::getName)
            .collect(Collectors.toSet());
        return currentAuthenticatedUserName()
            .map(contributorNames::contains)
            .defaultIfEmpty(false);
    }

    Mono<String> currentAuthenticatedUserName() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .filter(name -> !AnonymousUserConst.isAnonymousUser(name));
    }
}
