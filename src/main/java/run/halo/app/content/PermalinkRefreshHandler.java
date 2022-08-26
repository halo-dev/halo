package run.halo.app.content;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.PropertyPlaceholderHelper;
import run.halo.app.core.extension.Category;
import run.halo.app.core.extension.Post;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.theme.DefaultTemplateEnum;

/**
 * Permalink refresh handler.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PermalinkRefreshHandler implements ApplicationListener<PermalinkRuleChangedEvent> {
    private final ExtensionClient client;

    public PermalinkRefreshHandler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public void onApplicationEvent(@NonNull PermalinkRuleChangedEvent event) {
        DefaultTemplateEnum template = event.getTemplate();
        PermalinkUpdater permalinkUpdater = permalinkUpdater(template);
        if (permalinkUpdater == null) {
            return;
        }
        permalinkUpdater.update(event.getRule());
    }

    private PermalinkUpdater permalinkUpdater(DefaultTemplateEnum template) {
        return switch (template) {
            case POST -> new PostPermalinkUpdater(client);
            case CATEGORIES -> new CategoryPermalinkUpdater(client);
            case TAGS -> new TagPermalinkUpdater(client);
            default -> null;
        };
    }

    interface PermalinkUpdater {
        void update(String rule);
    }

    static class PostPermalinkUpdater implements PermalinkUpdater {
        private static final PropertyPlaceholderHelper propertyPlaceholderHelper =
            new PropertyPlaceholderHelper("{", "}");
        private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("00");
        private final ExtensionClient client;

        PostPermalinkUpdater(ExtensionClient client) {
            this.client = client;
        }

        @Override
        public void update(String pattern) {
            client.list(Post.class, null, null)
                .forEach(post -> {
                    String permalink = permalink(post, pattern);
                    post.getStatusOrDefault().setPermalink(permalink);
                    client.update(post);
                });
        }

        String permalink(Post post, String pattern) {
            Instant publishTime = post.getSpec().getPublishTime();
            ZonedDateTime zonedDateTime = publishTime.atZone(ZoneId.systemDefault());
            Properties properties = new Properties();
            properties.put("name", post.getMetadata().getName());
            properties.put("slug", post.getSpec().getSlug());
            properties.put("year", String.valueOf(zonedDateTime.getYear()));
            properties.put("month", NUMBER_FORMAT.format(zonedDateTime.getMonthValue()));
            properties.put("day", NUMBER_FORMAT.format(zonedDateTime.getDayOfMonth()));
            return propertyPlaceholderHelper.replacePlaceholders(pattern, properties);
        }
    }

    static class TagPermalinkUpdater implements PermalinkUpdater {
        private final ExtensionClient client;

        TagPermalinkUpdater(ExtensionClient client) {
            this.client = client;
        }

        @Override
        public void update(String rule) {
            client.list(Tag.class, null, null)
                .forEach(tag -> {
                    String permalink = PathUtils.combinePath(rule, tag.getSpec().getSlug());
                    tag.getStatusOrDefault().setPermalink(permalink);
                    client.update(tag);
                });
        }
    }

    static class CategoryPermalinkUpdater implements PermalinkUpdater {
        private final ExtensionClient client;

        CategoryPermalinkUpdater(ExtensionClient client) {
            this.client = client;
        }

        @Override
        public void update(String rule) {
            client.list(Category.class, null, null)
                .forEach(category -> {
                    String permalink = PathUtils.combinePath(rule, category.getSpec().getSlug());
                    category.getStatusOrDefault().setPermalink(permalink);
                    client.update(category);
                });
        }
    }
}
