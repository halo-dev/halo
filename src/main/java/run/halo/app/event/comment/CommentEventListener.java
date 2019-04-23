package run.halo.app.event.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.model.entity.Comment;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.service.CommentService;
import run.halo.app.service.MailService;
import run.halo.app.service.OptionService;

/**
 * Comment event listener.
 *
 * @author johnniang
 * @date 19-4-23
 */
@Slf4j
@Component
public class CommentEventListener {

    private final MailService mailService;

    private final OptionService optionService;

    private final CommentService commentService;

    public CommentEventListener(MailService mailService,
                                OptionService optionService,
                                CommentService commentService) {
        this.mailService = mailService;
        this.optionService = optionService;
        this.commentService = commentService;
    }

    @Async
    @EventListener
    public void handleCommentNewEvent(CommentNewEvent newEvent) {
        Boolean newCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.NEW_NOTICE, Boolean.class, false);

        if (!newCommentNotice) {
            // Skip mailing
            return;
        }

        // Get comment id
        Comment comment = commentService.getById(newEvent.getCommentId());

        // TODO Complete mail sending
    }

    @Async
    @EventListener
    public void handleCommentPassEvent(CommentPassEvent passEvent) {
        Boolean passCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.PASS_NOTICE, Boolean.class, false);

        if (!passCommentNotice) {
            // Skip mailing
            return;
        }

        // Get comment id
        Comment comment = commentService.getById(passEvent.getCommentId());

        // TODO Complete mail sending
    }

    @Async
    @EventListener
    public void handleCommentReplyEvent(CommentReplyEvent replyEvent) {
        Boolean replyCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.REPLY_NOTICE, Boolean.class, false);

        if (!replyCommentNotice) {
            // Skip mailing
            return;
        }

        // Get comment id
        Comment comment = commentService.getById(replyEvent.getCommentId());

        // TODO Complete mail sending
    }

}
