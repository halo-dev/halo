package run.halo.app.search.post;

import java.time.Instant;
import org.springframework.util.Assert;
import run.halo.app.theme.finders.vo.PostVo;

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

    // TODO Move this static method to other place.
    public static PostDoc from(PostVo postVo) {
        return new PostDoc(
            postVo.getMetadata().getName(),
            postVo.getSpec().getTitle(),
            postVo.getStatus().getExcerpt(),
            postVo.getContent().getContent(),
            postVo.getSpec().getPublishTime(),
            postVo.getStatus().getPermalink()
        );
    }
}
