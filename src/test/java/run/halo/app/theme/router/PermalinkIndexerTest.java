package run.halo.app.theme.router;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.content.permalinks.ExtensionLocator;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.GroupVersionKind;

/**
 * Tests for {@link PermalinkIndexer}.
 *
 * @author guqing
 * @since 2.0.0
 */
class PermalinkIndexerTest {

    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(FakeExtension.class);

    private PermalinkIndexer permalinkIndexer;

    @BeforeEach
    void setUp() {
        permalinkIndexer = new PermalinkIndexer();

        ExtensionLocator locator = new ExtensionLocator(gvk, "fake-name", "fake-slug");
        permalinkIndexer.register(locator, "/fake-permalink");
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(1);
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(1);
    }

    @Test
    void register() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(2);
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(2);
    }

    @Test
    void remove() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "fake-name", "fake-slug");

        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(1);
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(1);

        permalinkIndexer.remove(locator, "/fake-permalink");
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(0);
        assertThat(permalinkIndexer.permalinkLocatorMapSize()).isEqualTo(0);
    }

    @Test
    void lookup() {
        ExtensionLocator lookup = permalinkIndexer.lookup("/fake-permalink");
        assertThat(lookup).isEqualTo(new ExtensionLocator(gvk, "fake-name", "fake-slug"));

        lookup = permalinkIndexer.lookup("/nothing");
        assertThat(lookup).isNull();
    }

    @Test
    void getPermalinks() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        List<String> permalinks = permalinkIndexer.getPermalinks(gvk);
        assertThat(permalinks).isEqualTo(List.of("/fake-permalink", "/test-permalink"));
    }

    @Test
    void getNames() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        List<String> names = permalinkIndexer.getNames(gvk);
        assertThat(names).isEqualTo(List.of("fake-name", "test-name"));
    }

    @Test
    void getSlugs() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        List<String> slugs = permalinkIndexer.getSlugs(gvk);
        assertThat(slugs).isEqualTo(List.of("fake-slug", "test-slug"));
    }

    @Test
    void getSlugByName() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        String slugByName = permalinkIndexer.getSlugByName(gvk, "test-name");
        assertThat(slugByName).isEqualTo("test-slug");

        slugByName = permalinkIndexer.getSlugByName(gvk, "fake-name");
        assertThat(slugByName).isEqualTo("fake-slug");

        assertThatThrownBy(() -> {
            permalinkIndexer.getSlugByName(gvk, "nothing");
        }).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void getNameBySlug() {
        ExtensionLocator locator = new ExtensionLocator(gvk, "test-name", "test-slug");
        permalinkIndexer.register(locator, "/test-permalink");

        String nameBySlug = permalinkIndexer.getNameBySlug(gvk, "test-slug");
        assertThat(nameBySlug).isEqualTo("test-name");

        nameBySlug = permalinkIndexer.getNameBySlug(gvk, "fake-slug");
        assertThat(nameBySlug).isEqualTo("fake-name");

        assertThatThrownBy(() -> {
            permalinkIndexer.getNameBySlug(gvk, "nothing");
        }).isInstanceOf(NoSuchElementException.class);
    }
}