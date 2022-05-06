package run.halo.app.listener.comment;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import run.halo.app.event.comment.CommentNewEvent;
import run.halo.app.event.comment.CommentReplyEvent;
import run.halo.app.exception.ServiceException;
import run.halo.app.mail.MailService;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Journal;
import run.halo.app.model.entity.JournalComment;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.SheetComment;
import run.halo.app.model.entity.User;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.service.JournalCommentService;
import run.halo.app.service.JournalService;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostCommentService;
import run.halo.app.service.PostService;
import run.halo.app.service.SheetCommentService;
import run.halo.app.service.SheetService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.UserService;
import run.halo.app.service.assembler.PostAssembler;
import run.halo.app.service.assembler.SheetAssembler;
import run.halo.app.utils.ValidationUtils;

/**
 * PostComment event listener.
 *
 * @author johnniang
 * @author ryanwang
 * @date 2019-04-23
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

    private final PostAssembler postAssembler;

    private final SheetService sheetService;

    private final SheetAssembler sheetAssembler;

    private final JournalService journalService;

    private final UserService userService;

    private final ThemeService themeService;

    public CommentEventListener(MailService mailService, OptionService optionService,
        PostCommentService postCommentService, SheetCommentService sheetCommentService,
        JournalCommentService journalCommentService, PostService postService,
        PostAssembler postAssembler, SheetService sheetService,
        SheetAssembler sheetAssembler, JournalService journalService,
        UserService userService,
        ThemeService themeService) {
        this.mailService = mailService;
        this.optionService = optionService;
        this.postCommentService = postCommentService;
        this.sheetCommentService = sheetCommentService;
        this.journalCommentService = journalCommentService;
        this.postService = postService;
        this.postAssembler = postAssembler;
        this.sheetService = sheetService;
        this.sheetAssembler = sheetAssembler;
        this.journalService = journalService;
        this.userService = userService;
        this.themeService = themeService;
    }

    /**
     * Received a new comment event.
     *
     * @param newEvent new comment event.
     */
    @Async
    @TransactionalEventListener
    public void handleCommentNewEvent(CommentNewEvent newEvent) {
        Boolean newCommentNotice = optionService
            .getByPropertyOrDefault(CommentProperties.NEW_NOTICE, Boolean.class, false);

        if (!newCommentNotice) {
            // Skip mailing
            return;
        }

        User user =
            userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));

        Map<String, Object> data = new HashMap<>();

        StringBuilder subject = new StringBuilder();

        Boolean enabledAbsolutePath = optionService.isEnabledAbsolutePath();

        if (newEvent.getSource() instanceof PostCommentService) {
            // Get postComment id
            PostComment postComment = postCommentService.getById(newEvent.getCommentId());

            log.debug("Got post comment: [{}]", postComment);

            BasePostMinimalDTO post =
                postAssembler.convertToMinimal(postService.getById(postComment.getPostId()));

            data.put("pageFullPath", enabledAbsolutePath ? post.getFullPath() :
                optionService.getBlogBaseUrl() + post.getFullPath());
            data.put("pageTitle", post.getTitle());
            data.put("author", postComment.getAuthor());
            data.put("content", postComment.getContent());

            subject.append("您的博客文章《")
                .append(post.getTitle())
                .append("》有了新的评论。");

        } else if (newEvent.getSource() instanceof SheetCommentService) {
            SheetComment sheetComment = sheetCommentService.getById(newEvent.getCommentId());

            log.debug("Got sheet comment: [{}]", sheetComment);

            BasePostMinimalDTO sheet =
                sheetAssembler.convertToMinimal(sheetService.getById(sheetComment.getPostId()));

            data.put("pageFullPath", enabledAbsolutePath ? sheet.getFullPath() :
                optionService.getBlogBaseUrl() + sheet.getFullPath());
            data.put("pageTitle", sheet.getTitle());
            data.put("author", sheetComment.getAuthor());
            data.put("content", sheetComment.getContent());

            subject.append("您的博客页面《")
                .append(sheet.getTitle())
                .append("》有了新的评论。");
        } else if (newEvent.getSource() instanceof JournalCommentService) {
            JournalComment journalComment = journalCommentService.getById(newEvent.getCommentId());

            log.debug("Got journal comment: [{}]", journalComment);

            Journal journal = journalService.getById(journalComment.getPostId());

            StringBuilder url = new StringBuilder(optionService.getBlogBaseUrl())
                .append("/")
                .append(optionService.getJournalsPrefix());
            data.put("pageFullPath", url);
            data.put("pageTitle", journal.getCreateTime());
            data.put("author", journalComment.getAuthor());
            data.put("content", journalComment.getContent());

            subject.append("您的博客日志有了新的评论");
        }

        String template = "common/mail_template/mail_notice.ftl";

        if (themeService.templateExists("mail_template/mail_notice.ftl")) {
            template = themeService.renderWithSuffix("mail_template/mail_notice");
        }

        mailService.sendTemplateMail(user.getEmail(), subject.toString(), data, template);
    }

    /**
     * Received a new reply comment event.
     *
     * @param replyEvent reply comment event.
     */
    @Async
    @TransactionalEventListener
    public void handleCommentReplyEvent(CommentReplyEvent replyEvent) {
        Boolean replyCommentNotice = optionService
            .getByPropertyOrDefault(CommentProperties.REPLY_NOTICE, Boolean.class, false);

        if (!replyCommentNotice) {
            // Skip mailing
            return;
        }

        String baseAuthorEmail = "";

        String blogTitle = optionService.getBlogTitle();

        Map<String, Object> data = new HashMap<>();

        StringBuilder subject = new StringBuilder();

        Boolean enabledAbsolutePath = optionService.isEnabledAbsolutePath();

        log.debug("replyEvent.getSource():" + replyEvent.getSource().toString());

        if (replyEvent.getSource() instanceof PostCommentService) {

            PostComment postComment = postCommentService.getById(replyEvent.getCommentId());

            PostComment baseComment = postCommentService.getById(postComment.getParentId());

            if (StringUtils.isEmpty(baseComment.getEmail())
                && !ValidationUtils.isEmail(baseComment.getEmail())) {
                return;
            }

            if (!baseComment.getAllowNotification()) {
                return;
            }

            baseAuthorEmail = baseComment.getEmail();

            BasePostMinimalDTO post =
                postAssembler.convertToMinimal(postService.getById(postComment.getPostId()));

            data.put("pageFullPath", enabledAbsolutePath ? post.getFullPath() :
                optionService.getBlogBaseUrl() + post.getFullPath());
            data.put("pageTitle", post.getTitle());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", postComment.getAuthor());
            data.put("replyContent", postComment.getContent());

            subject.append("您在【")
                .append(blogTitle)
                .append("】评论的文章《")
                .append(post.getTitle())
                .append("》有了新的评论。");
        } else if (replyEvent.getSource() instanceof SheetCommentService) {

            SheetComment sheetComment = sheetCommentService.getById(replyEvent.getCommentId());

            SheetComment baseComment = sheetCommentService.getById(sheetComment.getParentId());

            if (StringUtils.isEmpty(baseComment.getEmail())
                && !ValidationUtils.isEmail(baseComment.getEmail())) {
                return;
            }

            if (!baseComment.getAllowNotification()) {
                return;
            }

            baseAuthorEmail = baseComment.getEmail();

            BasePostMinimalDTO sheet =
                sheetAssembler.convertToMinimal(sheetService.getById(sheetComment.getPostId()));

            data.put("pageFullPath", enabledAbsolutePath ? sheet.getFullPath() :
                optionService.getBlogBaseUrl() + sheet.getFullPath());
            data.put("pageTitle", sheet.getTitle());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", sheetComment.getAuthor());
            data.put("replyContent", sheetComment.getContent());

            subject.append("您在【")
                .append(blogTitle)
                .append("】评论的页面《")
                .append(sheet.getTitle())
                .append("》有了新的评论。");
        } else if (replyEvent.getSource() instanceof JournalCommentService) {
            JournalComment journalComment =
                journalCommentService.getById(replyEvent.getCommentId());

            JournalComment baseComment =
                journalCommentService.getById(journalComment.getParentId());

            if (StringUtils.isEmpty(baseComment.getEmail())
                && !ValidationUtils.isEmail(baseComment.getEmail())) {
                return;
            }

            if (!baseComment.getAllowNotification()) {
                return;
            }

            baseAuthorEmail = baseComment.getEmail();

            Journal journal = journalService.getById(journalComment.getPostId());

            StringBuilder url = new StringBuilder(optionService.getBlogBaseUrl())
                .append("/")
                .append(optionService.getJournalsPrefix());
            data.put("pageFullPath", url);
            data.put("pageTitle", journal.getContent());
            data.put("baseAuthor", baseComment.getAuthor());
            data.put("baseContent", baseComment.getContent());
            data.put("replyAuthor", journalComment.getAuthor());
            data.put("replyContent", journalComment.getContent());

            subject.append("您在【")
                .append(blogTitle)
                .append("】评论的日志")
                .append("有了新的评论。");
        }

        String template = "common/mail_template/mail_reply.ftl";

        if (themeService.templateExists("mail_template/mail_reply.ftl")) {
            template = themeService.renderWithSuffix("mail_template/mail_reply");
        }

        mailService.sendTemplateMail(baseAuthorEmail, subject.toString(), data, template);
    }
}
