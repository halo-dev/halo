package run.halo.app.content.permalinks;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.Post;
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
public class PostPermalinkPolicy implements PermalinkPolicy<Post>, PermalinkWatch<Post> {
    private final GroupVersionKind gvk = GroupVersionKind.fromExtension(Post.class);
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");

    private final PermalinkPatternProvider permalinkPatternProvider;
    private final ApplicationContext applicationContext;

    public PostPermalinkPolicy(PermalinkPatternProvider permalinkPatternProvider,
        ApplicationContext applicationContext) {
        this.permalinkPatternProvider = permalinkPatternProvider;
        this.applicationContext = applicationContext;
    }

    @Override
    public String permalink(Post post) {
        return createPermalink(post, pattern());
    }

    @Override
    public String templateName() {
        return DefaultTemplateEnum.POST.getValue();
    }

    @Override
    public String pattern() {
        return permalinkPatternProvider.getPattern(DefaultTemplateEnum.POST);
    }

    @Override
    public void onPermalinkAdd(Post post) {
        applicationContext.publishEvent(new PermalinkIndexAddCommand(this, getLocator(post),
            post.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkUpdate(Post post) {
        applicationContext.publishEvent(new PermalinkIndexUpdateCommand(this, getLocator(post),
            post.getStatusOrDefault().getPermalink()));
    }

    @Override
    public void onPermalinkDelete(Post post) {
        applicationContext.publishEvent(new PermalinkIndexDeleteCommand(this, getLocator(post)));
    }

    private ExtensionLocator getLocator(Post post) {
        return new ExtensionLocator(gvk, post.getMetadata().getName(), post.getSpec().getSlug());
    }

    private String createPermalink(Post post, String pattern) {
        Instant archiveTime = post.getSpec().getPublishTime();
        if (archiveTime == null) {
            archiveTime = post.getMetadata().getCreationTimestamp();
        }
        ZonedDateTime zonedDateTime = archiveTime.atZone(ZoneId.systemDefault());
        Properties properties = new Properties();
        properties.put("name", post.getMetadata().getName());
        properties.put("slug", post.getSpec().getSlug());
        properties.put("year", String.valueOf(zonedDateTime.getYear()));
        properties.put("month", NUMBER_FORMAT.format(zonedDateTime.getMonthValue()));
        properties.put("day", NUMBER_FORMAT.format(zonedDateTime.getDayOfMonth()));

        String simplifiedPattern = PathUtils.simplifyPathPattern(pattern);
        return PROPERTY_PLACEHOLDER_HELPER.replacePlaceholders(simplifiedPattern, properties);
    }
}
