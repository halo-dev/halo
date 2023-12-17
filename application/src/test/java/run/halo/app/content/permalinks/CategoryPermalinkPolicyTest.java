package run.halo.app.content.permalinks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.halo.app.core.extension.content.Category;
import run.halo.app.extension.Metadata;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;

/**
 * Tests for {@link CategoryPermalinkPolicy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class CategoryPermalinkPolicyTest {

    @Mock
    private ExternalUrlSupplier externalUrlSupplier;

    @Mock
    private SystemConfigurableEnvironmentFetcher environmentFetcher;

    private CategoryPermalinkPolicy categoryPermalinkPolicy;

    @BeforeEach
    void setUp() {
        categoryPermalinkPolicy =
            new CategoryPermalinkPolicy(externalUrlSupplier, environmentFetcher);
    }

    @Test
    void permalink() {
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
}