package run.halo.app.service.impl;

import cn.hutool.core.text.StrBuilder;
import freemarker.template.Template;
import io.github.biezhi.ome.OhMyEmail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import run.halo.app.exception.EmailException;
import run.halo.app.model.properties.EmailProperties;
import run.halo.app.service.MailService;
import run.halo.app.service.OptionService;

import java.io.File;
import java.util.Map;
import java.util.Properties;

/**
 * Mail service implementation.
 *
 * @author ryanwang
 * @date 2019-03-17
 */
@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final FreeMarkerConfigurer freeMarker;

    private final OptionService optionService;

    public MailServiceImpl(FreeMarkerConfigurer freeMarker,
                           OptionService optionService) {
        this.freeMarker = freeMarker;
        this.optionService = optionService;

    }

    @Override
    public void sendMail(String to, String subject, String content) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME).toString();

        try {
            OhMyEmail.subject(subject)
                    .from(fromUsername)
                    .to(to)
                    .text(content)
                    .send();
        } catch (Exception e) {
            log.debug("Email properties: to username: [{}], from username: [{}], subject: [{}], content: [{}]",
                    to, fromUsername, subject, content);
            throw new EmailException("发送邮件到 " + to + " 失败，请检查 SMTP 服务配置是否正确", e);
        }
    }

    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME).toString();

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
            throw new EmailException("发送模板邮件到 " + to + " 失败，请检查 SMTP 服务配置是否正确", e);
        }
    }

    @Override
    public void sendAttachMail(String to, String subject, Map<String, Object> content, String templateName, String attachFilename) {
        loadConfig();

        String fromUsername = optionService.getByPropertyOfNonNull(EmailProperties.FROM_NAME).toString();

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
            throw new EmailException("发送附件邮件到 " + to + " 失败，请检查 SMTP 服务配置是否正确", e);
        }
    }

    /**
     * Loads email config.
     */
    private void loadConfig() {
        // Get default properties
        Properties defaultProperties = OhMyEmail.defaultConfig(log.isDebugEnabled());

        // Set smtp host
        defaultProperties.setProperty("mail.smtp.host", optionService.getByPropertyOfNonNull(EmailProperties.HOST).toString());
        defaultProperties.setProperty("mail.transport.protocol", optionService.getByPropertyOrDefault(EmailProperties.PROTOCOL, String.class, EmailProperties.PROTOCOL.defaultValue()));
        defaultProperties.setProperty("mail.smtp.port", optionService.getByPropertyOrDefault(EmailProperties.SSL_PORT, String.class, EmailProperties.SSL_PORT.defaultValue()));

        // Config email
        OhMyEmail.config(defaultProperties,
                optionService.getByPropertyOfNonNull(EmailProperties.USERNAME).toString(),
                optionService.getByPropertyOfNonNull(EmailProperties.PASSWORD).toString());
    }

}
