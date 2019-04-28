package run.halo.app.event.comment;

import cn.hutool.core.text.StrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.User;
import run.halo.app.model.properties.BlogProperties;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * PostComment event listener.
 *
 * @author johnniang
 * @date 19-4-23
 */
@Slf4j
@Component
public class CommentEventListener {

    private final MailService mailService;

    private final OptionService optionService;

    private final PostCommentService postCommentService;

    private final PostService postService;

    private final UserService userService;

    public CommentEventListener(MailService mailService,
                                OptionService optionService,
                                PostCommentService postCommentService,
                                PostService postService,
                                UserService userService) {
        this.mailService = mailService;
        this.optionService = optionService;
        this.postCommentService = postCommentService;
        this.postService = postService;
        this.userService = userService;
    }

    @Async
    @EventListener
    public void handleCommentNewEvent(CommentNewEvent newEvent) {
        Boolean newCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.NEW_NOTICE, Boolean.class, false);

        if (!newCommentNotice) {
            // Skip mailing
            return;
        }

        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("Can not find blog owner"));

        // Get postComment id
        PostComment postComment = postCommentService.getById(newEvent.getCommentId());

        Post post = postService.getById(postComment.getPostId());

        Map<String, Object> data = new HashMap<>();

        StrBuilder url = new StrBuilder(optionService.getByPropertyOfNullable(BlogProperties.BLOG_URL).toString())
                .append("/archives/")
                .append(post.getUrl());
        data.put("url", url.toString());
        data.put("page", post.getTitle());
        data.put("author", postComment.getAuthor());
        data.put("content", postComment.getContent());
        mailService.sendTemplateMail(user.getEmail(), "您的博客有新的评论", data, "common/mail_template/mail_notice.ftl");
    }

    @Async
    @EventListener
    public void handleCommentPassEvent(CommentPassEvent passEvent) {
        Boolean passCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.PASS_NOTICE, Boolean.class, false);

        if (!passCommentNotice) {
            // Skip mailing
            return;
        }

        // Get postComment id
        PostComment postComment = postCommentService.getById(passEvent.getCommentId());

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

        // Get postComment id
        PostComment postComment = postCommentService.getById(replyEvent.getCommentId());

        // TODO Complete mail sending
    }

}
