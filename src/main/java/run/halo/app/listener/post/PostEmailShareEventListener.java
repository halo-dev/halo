package run.halo.app.listener.post;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import run.halo.app.event.post.PostEmailShareEvent;
import run.halo.app.exception.ServiceException;
import run.halo.app.mail.MailService;
import run.halo.app.model.dto.EmailDTO;
import run.halo.app.model.dto.post.BasePostMinimalDTO;
import run.halo.app.model.entity.Email;
import run.halo.app.model.entity.Post;
import run.halo.app.model.entity.PostComment;
import run.halo.app.model.entity.User;
import run.halo.app.model.properties.CommentProperties;
import run.halo.app.service.OptionService;
import run.halo.app.service.PostService;
import run.halo.app.service.ThemeService;
import run.halo.app.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Visit event listener.
 *
 * @author johnniang
 * @date 19-4-22
 */
@Slf4j
@Component
public class PostEmailShareEventListener {

    private final OptionService optionService;
    private final PostService postService;
    private final MailService mailService;
    private final ThemeService themeService;
    private final UserService userService;

    public PostEmailShareEventListener(PostService postService,
                                       UserService userService,
                                       MailService mailService,
                                       ThemeService themeService,
                                       OptionService optionService) {
        this.optionService = optionService;
        this.postService = postService;
        this.mailService = mailService;
        this.themeService = themeService;
        this.userService = userService;
    }


    /**
     * Received a new new comment event.
     *
     * @param shareEvent new comment event.
     */
    @Async
    @EventListener
    public void handleCommentNewEvent(PostEmailShareEvent shareEvent) {
        Boolean emailShare = optionService.getByPropertyOrDefault(CommentProperties.EMAIL_SHARE, Boolean.class, false);

        if (!emailShare) {
            // Skip mailing
            return;
        }
        List<EmailDTO> emails = shareEvent.getEmails();

        if (CollectionUtils.isEmpty(emails)) {
            return;
        }
        User user = userService.getCurrentUser().orElseThrow(() -> new ServiceException("未查询到博主信息"));
        Map<String, Object> data = new HashMap<>(8);

        Boolean enabledAbsolutePath = optionService.isEnabledAbsolutePath();
        StringBuilder subject = new StringBuilder();

        // Get post
        Post post = postService.getById(shareEvent.getPostId());
        log.debug("Got post: [{}]", post);

        BasePostMinimalDTO minimalDTO = postService.convertToMinimal(post);

        data.put("blogBasePath", optionService.getBlogBaseUrl());
        data.put("pageFullPath", enabledAbsolutePath ? minimalDTO.getFullPath() : optionService.getBlogBaseUrl() + minimalDTO.getFullPath());
        data.put("pageTitle", post.getTitle());
        data.put("author", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("pageContent", post.getSummary());
        data.put("emailCancelUrl", "javascript:alert('当前功能未开发，请联系管理员！！');");

        subject.append("您的朋友有新的文章《")
                .append(post.getTitle())
                .append("》");

        String template = "common/mail_template/mail_share.ftl";

        if (themeService.templateExists("mail_template/mail_share.ftl")) {
            template = themeService.renderWithSuffix("mail_template/mail_share");
        }

        // send email
        for (EmailDTO email : emails) {
            mailService.sendTemplateMail(email.getValue(), subject.toString(), data, template);
        }

    }

}
