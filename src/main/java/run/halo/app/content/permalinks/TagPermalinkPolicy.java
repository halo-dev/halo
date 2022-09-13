package run.halo.app.content.permalinks;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Tag;
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
public class TagPermalinkPolicy implements PermalinkPolicy<Tag>, PermalinkWatch<Tag> {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Tag.class);
    private final PermalinkPatternProvider permalinkPatternProvider;
    private final ApplicationContext applicationContext;

    public TagPermalinkPolicy(PermalinkPatternProvider permalinkPatternProvider,
        ApplicationContext applicationContext) {
        this.permalinkPatternProvider = permalinkPatternProvider;
        this.applicationContext = applicationContext;
    }

    @Override
    public String permalink(Tag tag) {
        return PathUtils.combinePath(pattern(), tag.getSpec().getSlug());
    }

    @Override
    public String templateName() {
        return DefaultTemplateEnum.TAG.getValue();
    }

    @Override
    public String pattern() {
        return permalinkPatternProvider.getPattern(DefaultTemplateEnum.TAG);
    }

    @Override
    public void onPermalinkAdd(Tag tag) {
        applicationContext.publishEvent(new PermalinkIndexAddCommand(this, getLocator(tag),
            tag.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkUpdate(Tag tag) {
        applicationContext.publishEvent(new PermalinkIndexUpdateCommand(this, getLocator(tag),
            tag.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkDelete(Tag tag) {
        applicationContext.publishEvent(new PermalinkIndexDeleteCommand(this, getLocator(tag)));
    }

    private ExtensionLocator getLocator(Tag tag) {
        return new ExtensionLocator(gvk, tag.getMetadata().getName(), tag.getSpec().getSlug());
    }
}
