package run.halo.app.theme.router;

import static run.halo.app.theme.utils.PatternUtils.normalizePattern;
import static run.halo.app.theme.utils.PatternUtils.normalizePostPattern;

import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.infra.SystemConfigChangedEvent;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.SystemSetting.ThemeRouteRules;

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
class ExtensionPermalinkPatternUpdater implements ApplicationListener<SystemConfigChangedEvent> {

    private final ExtensionClient client;

    @Override
    @Async
    public void onApplicationEvent(@NonNull SystemConfigChangedEvent event) {
        var oldData = event.getOldData();
        var newData = event.getNewData();
        var oldRules = SystemSetting.get(oldData, ThemeRouteRules.GROUP, ThemeRouteRules.class);
        if (oldRules == null) {
            oldRules = ThemeRouteRules.empty();
        }
        var newRules = SystemSetting.get(newData, ThemeRouteRules.GROUP, ThemeRouteRules.class);
        if (newRules == null) {
            newRules = ThemeRouteRules.empty();
        }
        var archivesRuleChanged = !Objects.equals(oldRules.getArchives(), newRules.getArchives());
        var postRuleChanged = !Objects.equals(oldRules.getPost(), newRules.getPost());
        var categoriesRuleChanged =
            !Objects.equals(oldRules.getCategories(), newRules.getCategories());
        if (categoriesRuleChanged) {
            var categoriesPattern = normalizePattern(newRules.getCategories());
            updateCategoryPermalink(categoriesPattern);
        }
        var tagsRuleChanged = !Objects.equals(oldRules.getTags(), newRules.getTags());
        if (tagsRuleChanged) {
            var tagsPattern = normalizePattern(newRules.getTags());
            log.info("Update tag permalink pattern for tags change: {}", tagsPattern);
            updateTagPermalink(tagsPattern);
        }
        if (archivesRuleChanged || categoriesRuleChanged || postRuleChanged) {
            var postPattern = normalizePostPattern(newRules);
            updatePostPermalink(postPattern);
        }
    }

    private void updatePostPermalink(String pattern) {
        log.debug("Update post permalink by new policy [{}]", pattern);
        // TODO Optimize by batch update
        client.listAll(Post.class, new ListOptions(), Sort.unsorted())
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
        client.listAll(Category.class, new ListOptions(), Sort.unsorted())
            .forEach(category -> updateIfPermalinkPatternChanged(category, pattern));
    }

    private void updateTagPermalink(String pattern) {
        log.debug("Update tag and tags permalink by new policy [{}]", pattern);
        client.listAll(Tag.class, new ListOptions(), Sort.unsorted())
            .forEach(tag -> updateIfPermalinkPatternChanged(tag, pattern));
    }
}
