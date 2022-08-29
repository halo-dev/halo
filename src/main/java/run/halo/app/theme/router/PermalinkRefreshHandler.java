package run.halo.app.theme.router;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.content.permalinks.PostPermalinkPolicy;
import run.halo.app.content.permalinks.TagPermalinkPolicy;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.strategy.TemplateRouterManager;

/**
 * Permalink refresh handler.
 *
 * @author guqing
 * @since 2.0.0
 */
@Slf4j
@Component
public class PermalinkRefreshHandler implements ApplicationListener<PermalinkRuleChangedEvent> {
    private final ExtensionClient client;
    private final TemplateRouterManager templateRouterManager;
    private final PostPermalinkPolicy postPermalinkPolicy;
    private final TagPermalinkPolicy tagPermalinkPolicy;
    private final CategoryPermalinkPolicy categoryPermalinkPolicy;

    public PermalinkRefreshHandler(ExtensionClient client,
        TemplateRouterManager templateRouterManager,
        PostPermalinkPolicy postPermalinkPolicy,
        TagPermalinkPolicy tagPermalinkPolicy,
        CategoryPermalinkPolicy categoryPermalinkPolicy) {
        this.client = client;
        this.templateRouterManager = templateRouterManager;
        this.postPermalinkPolicy = postPermalinkPolicy;
        this.tagPermalinkPolicy = tagPermalinkPolicy;
        this.categoryPermalinkPolicy = categoryPermalinkPolicy;
    }

    @Override
    public void onApplicationEvent(@NonNull PermalinkRuleChangedEvent event) {
        DefaultTemplateEnum template = event.getTemplate();
        log.debug("Refresh permalink for template [{}]", template.getValue());
        switch (template) {
            case POST -> updatePostPermalink();
            case ARCHIVES -> templateRouterManager.changeTemplatePattern(template.getValue());
            case CATEGORIES, CATEGORY -> updateCategoryPermalink();
            case TAGS, TAG -> updateTagPermalink();
            default -> {
            }
        }
    }

    private void updatePostPermalink() {
        String pattern = postPermalinkPolicy.pattern();
        log.debug("Update post permalink by new policy [{}]", pattern);
        client.list(Post.class, null, null)
            .forEach(post -> {
                String permalink = postPermalinkPolicy.permalink(post);
                post.getStatusOrDefault().setPermalink(permalink);
                // update permalink
                client.update(post);

                postPermalinkPolicy.onPermalinkUpdate(post);
                templateRouterManager.changeTemplatePattern(postPermalinkPolicy.templateName());
            });
    }

    private void updateCategoryPermalink() {
        String pattern = categoryPermalinkPolicy.pattern();
        log.debug("Update category permalink by new policy [{}]", pattern);
        client.list(Category.class, null, null)
            .forEach(category -> {
                String permalink = categoryPermalinkPolicy.permalink(category);
                category.getStatusOrDefault().setPermalink(permalink);
                // update permalink
                client.update(category);

                categoryPermalinkPolicy.onPermalinkUpdate(category);
                templateRouterManager.changeTemplatePattern(categoryPermalinkPolicy.templateName());
            });
    }

    private void updateTagPermalink() {
        String pattern = tagPermalinkPolicy.pattern();
        log.debug("Update tag permalink by new policy [{}]", pattern);
        client.list(Tag.class, null, null)
            .forEach(tag -> {
                String permalink = tagPermalinkPolicy.permalink(tag);
                tag.getStatusOrDefault().setPermalink(permalink);
                // update permalink
                client.update(tag);

                tagPermalinkPolicy.onPermalinkUpdate(tag);
                templateRouterManager.changeTemplatePattern(tagPermalinkPolicy.templateName());
            });
    }
}
