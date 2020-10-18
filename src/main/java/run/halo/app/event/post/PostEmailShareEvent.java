package run.halo.app.event.post;

import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.entity.Email;

import java.util.List;
import java.util.Set;

/**
 * PostEmail new event.
 *
 * @author johnniang
 * @date 19-4-23
 */
public class PostEmailShareEvent extends ApplicationEvent {

    private final Integer postId;


    private final List<EmailDTO> emails;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     * @param postId post id
     * @param emails email Ids
     */
    public PostEmailShareEvent(Object source, @NonNull Integer postId, @NonNull List<EmailDTO> emails) {

        super(source);

        Assert.notNull(postId, "PostComment id must not be null");
        Assert.notNull(emails, "PostComment id must not be null");
        this.postId = postId;
        this.emails = emails;
    }

    public Integer getPostId() {
        return postId;
    }

    public List<EmailDTO> getEmails() {
        return emails;
    }
}
