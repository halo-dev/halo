package run.halo.app.theme;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Properties;
import org.springframework.util.PropertyPlaceholderHelper;
import run.halo.app.core.extension.Post;

/**
 * @author guqing
 * @since 2.0.0
 */
public class PostPermalinkPolicy {
    private static final PropertyPlaceholderHelper propertyPlaceholderHelper =
        new PropertyPlaceholderHelper("{", "}");

    public String create(Post post, String pattern) {
        Instant publishTime = post.getSpec().getPublishTime();
        ZonedDateTime zonedDateTime = publishTime.atZone(ZoneId.systemDefault());
        Properties properties = new Properties();
        properties.put("name", post.getMetadata().getName());
        properties.put("slug", post.getSpec().getSlug());
        properties.put("year", String.valueOf(zonedDateTime.getYear()));
        properties.put("month", String.valueOf(zonedDateTime.getMonthValue()));
        properties.put("day", String.valueOf(zonedDateTime.getDayOfMonth()));
        propertyPlaceholderHelper.replacePlaceholders(pattern, properties);
        return null;
    }
}
