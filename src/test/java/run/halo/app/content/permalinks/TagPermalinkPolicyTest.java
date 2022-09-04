package run.halo.app.content.permalinks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.Metadata;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkPatternProvider;

/**
 * Tests for {@link TagPermalinkPolicy}.
 *
 * @author guqing
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
class TagPermalinkPolicyTest {

    @Mock
    private PermalinkPatternProvider permalinkPatternProvider;

    @Mock
    private ApplicationContext applicationContext;

    private TagPermalinkPolicy tagPermalinkPolicy;

    @BeforeEach
    void setUp() {
        tagPermalinkPolicy = new TagPermalinkPolicy(permalinkPatternProvider, applicationContext);
    }

    @Test
    void permalink() {
        when(permalinkPatternProvider.getPattern(eq(DefaultTemplateEnum.TAG)))
            .thenReturn("tags");

        Tag tag = new Tag();
        Metadata metadata = new Metadata();
        metadata.setName("test-tag");
        tag.setMetadata(metadata);
        Tag.TagSpec tagSpec = new Tag.TagSpec();
        tagSpec.setSlug("test-slug");
        tag.setSpec(tagSpec);

        String permalink = tagPermalinkPolicy.permalink(tag);
        assertThat(permalink).isEqualTo("/tags/test-slug");
    }

    @Test
    void templateName() {
        String s = tagPermalinkPolicy.templateName();
        assertThat(s).isEqualTo(DefaultTemplateEnum.TAG.getValue());
    }

    @Test
    void pattern() {
        when(permalinkPatternProvider.getPattern(eq(DefaultTemplateEnum.TAG)))
            .thenReturn("tags");
        assertThat(tagPermalinkPolicy.pattern()).isEqualTo("tags");
    }
}