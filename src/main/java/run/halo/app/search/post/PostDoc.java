package run.halo.app.search.post;

import java.time.Instant;
import lombok.Data;
import run.halo.app.theme.finders.vo.PostVo;

@Data
public class PostDoc {

    public static final String ID_FIELD = "name";

    private String name;

    private String title;

    private String excerpt;

    private String content;

    private Instant publishTimestamp;

    private String permalink;

    // TODO Move this static method to other place.
    public static PostDoc from(PostVo postVo) {
        var post = new PostDoc();
        post.setName(postVo.getMetadata().getName());
        post.setTitle(postVo.getSpec().getTitle());
        post.setExcerpt(postVo.getStatus().getExcerpt());
        post.setPublishTimestamp(postVo.getSpec().getPublishTime());
        post.setContent(postVo.getContent().getContent());
        post.setPermalink(postVo.getStatus().getPermalink());
        return post;
    }

}
