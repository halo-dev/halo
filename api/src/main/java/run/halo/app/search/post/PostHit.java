package run.halo.app.search.post;

import java.time.Instant;
import lombok.Data;

@Data
@Deprecated(forRemoval = true, since = "2.17")
public class PostHit {

    private String name;

    private String title;

    private String content;

    private Instant publishTimestamp;

    private String permalink;

}
