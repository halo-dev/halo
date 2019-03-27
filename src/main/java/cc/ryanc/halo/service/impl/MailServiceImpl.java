package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.exception.ServiceException;
import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.model.enums.EmailProperties;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.service.OptionService;
import cn.hutool.core.text.StrBuilder;
import freemarker.template.Template;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final FreeMarkerConfigurer freeMarker;

    private final OptionService optionService;

    private boolean loaded = false;

    public MailServiceImpl(FreeMarkerConfigurer freeMarker,
                           OptionService optionService) {
        this.freeMarker = freeMarker;
        this.optionService = optionService;

        try {
            reloadMailConfig();
        } catch (Exception e) {
            log.warn("You have to configure the email settings correctly before using email service");
        }
    }

    @Override
    public void reloadMailConfig() {
        loaded = false;
        // Get default properties
        loadConfig();
    }

    /**
     * Sends a simple email
     *
     * @param to      recipient
     * @param subject subject
     * @param content content
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME);

        try {
            OhMyEmail.subject(subject)
                    .from(fromUsername)
                    .to(to)
                    .text(content)
                    .send();
        } catch (Exception e) {
            log.debug("Email properties: to username: [{}], from username: [{}], subject: [{}], content: [{}]",
                    to, fromUsername, subject, content);
            throw new ServiceException("Failed to send email to " + to, e);
        }
    }

    /**
     * Sends template mail
     *
     * @param to           recipient
     * @param subject      subject
     * @param content      content
     * @param templateName template name
     */
    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME);

        try {
            StrBuilder text = new StrBuilder();
            Template template = freeMarker.getConfiguration().getTemplate(templateName);
            text.append(FreeMarkerTemplateUtils.processTemplateIntoString(template, content));
            OhMyEmail.subject(subject)
                    .from(fromUsername)
                    .to(to)
                    .html(text.toString())
                    .send();
        } catch (Exception e) {
            log.debug("Email properties: to username: [{}], from username: [{}], subject: [{}], template name: [{}], content: [{}]",
                    to, fromUsername, subject, templateName, content);
            throw new ServiceException("Failed to send template email to " + to, e).setErrorData(templateName);
        }
    }

    /**
     * Sends mail with attachments
     *
     * @param to             recipient
     * @param subject        subject
     * @param content        content
     * @param templateName   template name
     * @param attachFilename attachment path
     */
    @Override
    public void sendAttachMail(String to, String subject, Map<String, Object> content, String templateName, String attachFilename) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(BlogProperties.MAIL_FROM_NAME);

        File file = new File(attachFilename);
        try {
            Template template = freeMarker.getConfiguration().getTemplate(templateName);
            OhMyEmail.subject(subject)
                    .from(fromUsername)
                    .to(to)
                    .html(FreeMarkerTemplateUtils.processTemplateIntoString(template, content))
                    .attach(file, file.getName())
                    .send();
        } catch (Exception e) {
            log.debug("Email properties: to username: [{}], from username: [{}], subject: [{}], template name: [{}], attachment: [{}], content: [{}]",
                    to, fromUsername, subject, templateName, attachFilename, content);
            throw new ServiceException("Failed to send attachment email to " + to, e);
        }
    }

    /**
     * Load email config.
     */
    private synchronized void loadConfig() {
        if (loaded = true) {
            return;
        }

        // Get default properties
        Properties defaultProperties = OhMyEmail.defaultConfig(log.isDebugEnabled());
        // Set smtp host
        defaultProperties.setProperty("mail.smtp.host", optionService.getByPropertyOfNonNull(EmailProperties.SMTP_HOST));
        // Config email
        OhMyEmail.config(defaultProperties,
                optionService.getByPropertyOfNonNull(EmailProperties.SMTP_USERNAME),
                optionService.getByPropertyOfNonNull(EmailProperties.SMTP_PASSWORD));

        // Set config loaded with true
        loaded = true;
    }
}
