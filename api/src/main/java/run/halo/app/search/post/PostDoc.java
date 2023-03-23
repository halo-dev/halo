package run.halo.app.search.post;

import java.time.Instant;
import org.springframework.util.Assert;

public record PostDoc(String name,
                      String title,
                      String excerpt,
                      String content,
                      Instant publishTimestamp,
                      String permalink) {

    public static final String ID_FIELD = "name";

    public PostDoc {
        Assert.hasText(name, "Name must not be blank");
        Assert.hasText(title, "Title must not be blank");
        Assert.hasText(permalink, "Permalink must not be blank");
        Assert.notNull(publishTimestamp, "PublishTimestamp must not be null");
    }

}
