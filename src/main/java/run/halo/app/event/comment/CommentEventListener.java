package run.halo.app.event.comment;

import cn.hutool.core.text.StrBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import run.halo.app.exception.ServiceException;
import run.halo.app.model.entity.*;
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

    private final SheetCommentService sheetCommentService;

    private final JournalCommentService journalCommentService;

    private final PostService postService;

    private final SheetService sheetService;

    private final JournalService journalService;

    private final UserService userService;

    public CommentEventListener(MailService mailService, OptionService optionService, PostCommentService postCommentService, SheetCommentService sheetCommentService, JournalCommentService journalCommentService, PostService postService, SheetService sheetService, JournalService journalService, UserService userService) {
        this.mailService = mailService;
        this.optionService = optionService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.postService = postService;
        this.sheetService = sheetService;
        this.journalService = journalService;
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


        Map<String, Object> data = new HashMap<>();

        if (this instanceof PostService) {
            // Get postComment id
            PostComment postComment = postCommentService.getById(newEvent.getCommentId());

            Post post = postService.getById(postComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/archives/")
                    .append(post.getUrl());
            data.put("url", url.toString());
            data.put("page", post.getTitle());
            data.put("author", postComment.getAuthor());
            data.put("content", postComment.getContent());
        } else if (this instanceof SheetService) {
            SheetComment sheetComment = sheetCommentService.getById(newEvent.getCommentId());

            Sheet sheet = sheetService.getById(sheetComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/s/")
                    .append(sheet.getUrl());
            data.put("url", url.toString());
            data.put("page", sheet.getTitle());
            data.put("author", sheetComment.getAuthor());
            data.put("content", sheetComment.getContent());
        } else if (this instanceof JournalService) {
            JournalComment journalComment = journalCommentService.getById(newEvent.getCommentId());

            Journal journal = journalService.getById(journalComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/journals");
            data.put("url", url.toString());
            data.put("page", journal.getCreateTime());
            data.put("author", journalComment.getAuthor());
            data.put("content", journalComment.getContent());
        }

        mailService.sendTemplateMail(user.getEmail(), "您的博客有新的评论", data, "common/mail_template/mail_notice.ftl");
    }

    @Async
    @EventListener
    public void handleCommentPassEvent(CommentPassEvent passEvent) {

    }

    @Async
    @EventListener
    public void handleCommentReplyEvent(CommentReplyEvent replyEvent) {
        Boolean replyCommentNotice = optionService.getByPropertyOrDefault(CommentProperties.REPLY_NOTICE, Boolean.class, false);

        if (!replyCommentNotice) {
            // Skip mailing
            return;
        }

        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("Can not find blog owner"));

        String blogTitle = optionService.getBlogTitle();

        Map<String, Object> data = new HashMap<>();

        if (this instanceof PostService) {

            PostComment postComment = postCommentService.getById(replyEvent.getCommentId());

            PostComment baseComment = postCommentService.getById(postComment.getParentId());

            Post post = postService.getById(postComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/archives/")
                    .append(post.getUrl());

            data.put("url", url);
            data.put("page", post.getTitle());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", postComment.getAuthor());
            data.put("replyContent", postComment.getContent());
        } else if (this instanceof SheetService) {

            SheetComment sheetComment = sheetCommentService.getById(replyEvent.getCommentId());

            SheetComment baseComment = sheetCommentService.getById(sheetComment.getParentId());

            Sheet sheet = sheetService.getById(sheetComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/s/")
                    .append(sheet.getUrl());

            data.put("url", url);
            data.put("page", sheet.getTitle());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", sheetComment.getAuthor());
            data.put("replyContent", sheetComment.getContent());
        } else if (this instanceof JournalService) {
            JournalComment journalComment = journalCommentService.getById(replyEvent.getCommentId());

            JournalComment baseComment = journalCommentService.getById(journalComment.getParentId());

            Journal journal = journalService.getById(journalComment.getPostId());

            StrBuilder url = new StrBuilder(optionService.getBlogBaseUrl())
                    .append("/journals");
            data.put("url", url);
            data.put("page", journal.getContent());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", journalComment.getAuthor());
            data.put("replyContent", journalComment.getContent());
        }

        mailService.sendTemplateMail(user.getEmail(), "您在【" + blogTitle + "】的评论有新回复", data, "common/mail_template/mail_reply.ftl");
    }
}
