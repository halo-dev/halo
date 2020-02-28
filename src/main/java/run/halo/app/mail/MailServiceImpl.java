package run.halo.app.mail;

import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.event.options.OptionUpdatedEvent;
import run.halo.app.service.OptionService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Mail service implementation.
 *
 * @author ryanwang
 * @author johnniang
 * @date 2019-03-17
 */
@Slf4j
@Service
public class MailServiceImpl extends AbstractMailService implements ApplicationListener<OptionUpdatedEvent> {

    private final FreeMarkerConfigurer freeMarker;

    public MailServiceImpl(FreeMarkerConfigurer freeMarker,
                           OptionService optionService) {
        super(optionService);
        this.freeMarker = freeMarker;
    }

    @Override
    public void sendTextMail(String to, String subject, String content) {
        sendMailTemplate(true, messageHelper -> {
            messageHelper.setSubject(subject);
            messageHelper.setTo(to);
            messageHelper.setText(content);
        });
    }

    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName) {
        sendMailTemplate(true, messageHelper -> {
            // build message content with freemarker
            Template template = freeMarker.getConfiguration().getTemplate(templateName);
            String contentResult = FreeMarkerTemplateUtils.processTemplateIntoString(template, content);

            messageHelper.setSubject(subject);
            messageHelper.setTo(to);
            messageHelper.setText(contentResult, true);
        });
    }

    @Override
    public void sendAttachMail(String to, String subject, Map<String, Object> content, String templateName, String attachFilePath) {
        sendMailTemplate(true, messageHelper -> {
            messageHelper.setSubject(subject);
            messageHelper.setTo(to);
            Path attachmentPath = Paths.get(attachFilePath);
            messageHelper.addAttachment(attachmentPath.getFileName().toString(), attachmentPath.toFile());
        });
    }

    @Override
    public void testConnection() {
        super.testConnection();
    }


    @Override
    public void onApplicationEvent(OptionUpdatedEvent event) {
        // clear the cached java mail sender
        clearCache();
    }
}
