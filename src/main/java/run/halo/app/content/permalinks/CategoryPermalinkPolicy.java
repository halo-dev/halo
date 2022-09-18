package run.halo.app.content.permalinks;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Category;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;
import run.halo.app.theme.router.PermalinkIndexAddCommand;
import run.halo.app.theme.router.PermalinkIndexDeleteCommand;
import run.halo.app.theme.router.PermalinkIndexUpdateCommand;
import run.halo.app.theme.router.PermalinkPatternProvider;
import run.halo.app.theme.router.PermalinkWatch;

/**
 * @author guqing
 * @since 2.0.0
 */
@Component
public class CategoryPermalinkPolicy
    implements PermalinkPolicy<Category>, PermalinkWatch<Category> {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Category.class);
    private final ApplicationContext applicationContext;
    private final PermalinkPatternProvider permalinkPatternProvider;

    public CategoryPermalinkPolicy(ApplicationContext applicationContext,
        PermalinkPatternProvider permalinkPatternProvider) {
        this.applicationContext = applicationContext;
        this.permalinkPatternProvider = permalinkPatternProvider;
    }

    @Override
    public String permalink(Category category) {
        return PathUtils.combinePath(pattern(), category.getSpec().getSlug());
    }

    @Override
    public String templateName() {
        return DefaultTemplateEnum.CATEGORY.getValue();
    }

    @Override
    public String pattern() {
        return permalinkPatternProvider.getPattern(DefaultTemplateEnum.CATEGORY);
    }

    @Override
    public void onPermalinkAdd(Category category) {
        applicationContext.publishEvent(new PermalinkIndexAddCommand(this, getLocator(category),
            category.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkUpdate(Category category) {
        applicationContext.publishEvent(new PermalinkIndexUpdateCommand(this, getLocator(category),
            category.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkDelete(Category category) {
        applicationContext.publishEvent(
            new PermalinkIndexDeleteCommand(this, getLocator(category)));
    }

    private ExtensionLocator getLocator(Category category) {
        return new ExtensionLocator(gvk, category.getMetadata().getName(),
            category.getSpec().getSlug());
    }
}
