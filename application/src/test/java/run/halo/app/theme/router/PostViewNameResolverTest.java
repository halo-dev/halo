package run.halo.app.theme.router;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.ViewNameResolver;
import run.halo.app.theme.finders.vo.CategoryVo;
import run.halo.app.theme.finders.vo.PostVo;

@ExtendWith(MockitoExtension.class)
class PostViewNameResolverTest {

    @Mock
    ViewNameResolver viewNameResolver;

    @Mock
    ServerRequest request;

    @InjectMocks
    PostViewNameResolver postViewNameResolver;

    @Test
    void shouldResolvePostTemplateBeforeCategoryPostTemplate() {
        PostVo postVo = postVo("post-template", categoryVo("category-template"));
        when(viewNameResolver.resolveViewNameOrDefault(request, "post-template", null))
                .thenReturn(Mono.just("post-template"));

        postViewNameResolver
                .resolveViewNameOrDefault(request, postVo)
                .as(StepVerifier::create)
                .expectNext("post-template")
                .verifyComplete();

        verify(viewNameResolver).resolveViewNameOrDefault(request, "post-template", null);
        verifyNoMoreInteractions(viewNameResolver);
    }

    @Test
    void shouldResolveCategoryPostTemplateWhenPostTemplateIsBlank() {
        PostVo postVo = postVo("", categoryVo("category-template"));
        when(viewNameResolver.resolveViewNameOrDefault(request, "category-template", null))
                .thenReturn(Mono.just("category-template"));

        postViewNameResolver
                .resolveViewNameOrDefault(request, postVo)
                .as(StepVerifier::create)
                .expectNext("category-template")
                .verifyComplete();

        verify(viewNameResolver).resolveViewNameOrDefault(request, "category-template", null);
        verifyNoMoreInteractions(viewNameResolver);
    }

    @Test
    void shouldResolveDefaultPostTemplateWhenCustomTemplatesAreBlank() {
        PostVo postVo = postVo("", categoryVo(""));
        when(viewNameResolver.resolveViewNameOrDefault(request, null, DefaultTemplateEnum.POST.getValue()))
                .thenReturn(Mono.just(DefaultTemplateEnum.POST.getValue()));

        postViewNameResolver
                .resolveViewNameOrDefault(request, postVo)
                .as(StepVerifier::create)
                .expectNext(DefaultTemplateEnum.POST.getValue())
                .verifyComplete();

        verify(viewNameResolver).resolveViewNameOrDefault(request, null, DefaultTemplateEnum.POST.getValue());
        verifyNoMoreInteractions(viewNameResolver);
    }

    @Test
    void shouldFallBackToDefaultPostTemplateWhenCustomTemplatesCannotResolve() {
        PostVo postVo = postVo("missing-post-template", categoryVo("missing-category-template"));
        when(viewNameResolver.resolveViewNameOrDefault(request, "missing-post-template", null))
                .thenReturn(Mono.empty());
        when(viewNameResolver.resolveViewNameOrDefault(request, "missing-category-template", null))
                .thenReturn(Mono.empty());
        when(viewNameResolver.resolveViewNameOrDefault(request, null, DefaultTemplateEnum.POST.getValue()))
                .thenReturn(Mono.just(DefaultTemplateEnum.POST.getValue()));

        postViewNameResolver
                .resolveViewNameOrDefault(request, postVo)
                .as(StepVerifier::create)
                .expectNext(DefaultTemplateEnum.POST.getValue())
                .verifyComplete();

        verify(viewNameResolver).resolveViewNameOrDefault(request, "missing-post-template", null);
        verify(viewNameResolver).resolveViewNameOrDefault(request, "missing-category-template", null);
        verify(viewNameResolver).resolveViewNameOrDefault(request, null, DefaultTemplateEnum.POST.getValue());
        verifyNoMoreInteractions(viewNameResolver);
    }

    private PostVo postVo(String template, CategoryVo categoryVo) {
        Post post = new Post();
        post.setSpec(new Post.PostSpec());
        post.getSpec().setTemplate(template);
        PostVo postVo = PostVo.from(post);
        postVo.setCategories(List.of(categoryVo));
        return postVo;
    }

    private CategoryVo categoryVo(String postTemplate) {
        Category.CategorySpec spec = new Category.CategorySpec();
        spec.setPostTemplate(postTemplate);
        return CategoryVo.builder().spec(spec).build();
    }
}
