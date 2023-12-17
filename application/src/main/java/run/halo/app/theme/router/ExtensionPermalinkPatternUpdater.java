package run.halo.app.theme.router;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * {@link ExtensionPermalinkPatternUpdater} to update the value of key
 * {@link Constant#PERMALINK_PATTERN_ANNO} in {@link MetadataOperator#getAnnotations()}
 * of {@link Extension} when the pattern changed.
 *
 * @author guqing
 * @see Post
 * @see Category
 * @see Tag
 * @since 2.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExtensionPermalinkPatternUpdater
    implements ApplicationListener<PermalinkRuleChangedEvent> {
    private final ExtensionClient client;

    @Override
    public void onApplicationEvent(@NonNull PermalinkRuleChangedEvent event) {
        DefaultTemplateEnum template = event.getTemplate();
        log.debug("Refresh permalink for template [{}]", template.getValue());
        String pattern = event.getRule();
        switch (template) {
            case POST -> updatePostPermalink(pattern);
            case CATEGORY -> updateCategoryPermalink(pattern);
            case TAG -> updateTagPermalink(pattern);
            default -> {
            }
        }
    }

    private void updatePostPermalink(String pattern) {
        log.debug("Update post permalink by new policy [{}]", pattern);
        client.list(Post.class, null, null)
            .forEach(post -> updateIfPermalinkPatternChanged(post, pattern));
    }

    private void updateIfPermalinkPatternChanged(AbstractExtension extension, String pattern) {
        Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(extension);
        String oldPattern = annotations.get(Constant.PERMALINK_PATTERN_ANNO);
        annotations.put(Constant.PERMALINK_PATTERN_ANNO, pattern);

        if (StringUtils.equals(oldPattern, pattern) && StringUtils.isNotBlank(oldPattern)) {
            return;
        }
        // update permalink pattern annotation
        client.update(extension);
    }

    private void updateCategoryPermalink(String pattern) {
        log.debug("Update category and categories permalink by new policy [{}]", pattern);
        client.list(Category.class, null, null)
            .forEach(category -> updateIfPermalinkPatternChanged(category, pattern));
    }

    private void updateTagPermalink(String pattern) {
        log.debug("Update tag and tags permalink by new policy [{}]", pattern);
        client.list(Tag.class, null, null)
            .forEach(tag -> updateIfPermalinkPatternChanged(tag, pattern));
    }
}
