package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.enums.BlogProperties;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.utils.HaloUtils;
import cn.hutool.core.text.StrBuilder;
import freemarker.template.Template;
import io.github.biezhi.ome.OhMyEmail;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.util.Map;

import static cc.ryanc.halo.model.support.HaloConst.OPTIONS;

/**
 * @author : RYAN0UP
 * @date : 2019-03-17
 */
@Service
public class MailServiceImpl implements MailService {

    private final FreeMarkerConfigurer freeMarker;

    public MailServiceImpl(FreeMarkerConfigurer freeMarker) {
        this.freeMarker = freeMarker;
    }

    /**
     * Send a simple email
     *
     * @param to      recipient
     * @param subject subject
     * @param content content
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        HaloUtils.configMail(
                OPTIONS.get(BlogProperties.MAIL_SMTP_HOST.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_USERNAME.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_PASSWORD.getValue()));
        try {
            OhMyEmail.subject(subject)
                    .from(OPTIONS.get(BlogProperties.MAIL_FROM_NAME.getValue()))
                    .to(to)
                    .text(content)
                    .send();
        } catch (Exception e) {
            // TODO Handle this exception.
            e.printStackTrace();
        }
    }

    /**
     * Send template mail
     *
     * @param to           recipient
     * @param subject      subject
     * @param content      content
     * @param templateName template name
     */
    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName) {
        HaloUtils.configMail(
                OPTIONS.get(BlogProperties.MAIL_SMTP_HOST.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_USERNAME.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_PASSWORD.getValue()));
        StrBuilder text = new StrBuilder();
        try {
            final Template template = freeMarker.getConfiguration().getTemplate(templateName);
            text.append(FreeMarkerTemplateUtils.processTemplateIntoString(template, content));
            OhMyEmail.subject(subject)
                    .from(OPTIONS.get(BlogProperties.MAIL_FROM_NAME.getValue()))
                    .to(to)
                    .html(text.toString())
                    .send();
        } catch (Exception e) {
            // TODO Handle this exception.
            e.printStackTrace();
        }
    }

    /**
     * Send mail with attachments
     *
     * @param to           recipient
     * @param subject      subject
     * @param content      content
     * @param templateName template name
     * @param attachSrc    attachment path
     */
    @Override
    public void sendAttachMail(String to, String subject, Map<String, Object> content, String templateName, String attachSrc) {
        HaloUtils.configMail(
                OPTIONS.get(BlogProperties.MAIL_SMTP_HOST.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_USERNAME.getValue()),
                OPTIONS.get(BlogProperties.MAIL_SMTP_PASSWORD.getValue()));
        File file = new File(attachSrc);
        StrBuilder text = new StrBuilder();
        try {
            final Template template = freeMarker.getConfiguration().getTemplate(templateName);
            text.append(FreeMarkerTemplateUtils.processTemplateIntoString(template, content));
            OhMyEmail.subject(subject)
                    .from(OPTIONS.get(BlogProperties.MAIL_FROM_NAME.getValue()))
                    .to(to)
                    .html(text.toString())
                    .attach(file, file.getName())
                    .send();
        } catch (Exception e) {
            // TODO Handle this exception.
            e.printStackTrace();
        }
    }
}
