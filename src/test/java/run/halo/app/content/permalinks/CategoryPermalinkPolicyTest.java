package run.halo.app.content.permalinks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkPatternProvider;

/**
 * Tests for {@link CategoryPermalinkPolicy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryPermalinkPolicyTest {

    @Mock
    private PermalinkPatternProvider permalinkPatternProvider;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    private CategoryPermalinkPolicy categoryPermalinkPolicy;

    @BeforeEach
    void setUp() {
        categoryPermalinkPolicy =
            new CategoryPermalinkPolicy(applicationContext, permalinkPatternProvider,
                externalUrlSupplier);
    }

    @Test
    void permalink() {
        when(permalinkPatternProvider.getPattern(eq(DefaultTemplateEnum.CATEGORY)))
            .thenReturn("categories");
        Category category = new Category();
        Metadata metadata = new Metadata();
        metadata.setName("category-test");
        category.setMetadata(metadata);
        Category.CategorySpec categorySpec = new Category.CategorySpec();
        categorySpec.setSlug("slug-test");
        category.setSpec(categorySpec);

        when(externalUrlSupplier.get()).thenReturn(URI.create(""));
        String permalink = categoryPermalinkPolicy.permalink(category);
        assertThat(permalink).isEqualTo("/categories/slug-test");

        when(externalUrlSupplier.get()).thenReturn(URI.create("http://exmaple.com"));
        permalink = categoryPermalinkPolicy.permalink(category);
        assertThat(permalink).isEqualTo("http://exmaple.com/categories/slug-test");
        String path = URI.create(permalink).getPath();
        assertThat(path).isEqualTo("/categories/slug-test");

        category.getSpec().setSlug("中文 slug");
        permalink = categoryPermalinkPolicy.permalink(category);
        assertThat(permalink).isEqualTo("http://exmaple.com/categories/%E4%B8%AD%E6%96%87%20slug");
    }

    @Test
    void templateName() {
        String s = categoryPermalinkPolicy.templateName();
        assertThat(s).isEqualTo(DefaultTemplateEnum.CATEGORY.getValue());
    }

    @Test
    void pattern() {
        when(permalinkPatternProvider.getPattern(eq(DefaultTemplateEnum.CATEGORY)))
            .thenReturn("categories");
        String pattern = categoryPermalinkPolicy.pattern();
        assertThat(pattern).isEqualTo("categories");
    }
}