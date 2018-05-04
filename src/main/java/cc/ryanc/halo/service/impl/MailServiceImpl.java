package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.MailService;
import cc.ryanc.halo.util.HaloUtil;
import freemarker.template.Template;
import io.github.biezhi.ome.OhMyEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Map;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/23
 */
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private FreeMarkerConfigurer freeMarker;

    /**
     * 发送邮件
     *
     * @param to      to 接收者
     * @param subject subject 标题
     * @param content content 内容
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        //配置邮件服务器
        HaloUtil.configMail(
                HaloConst.OPTIONS.get("mail_smtp_host"),
                HaloConst.OPTIONS.get("mail_smtp_username"),
                HaloConst.OPTIONS.get("mail_smtp_password"));
        try {
            OhMyEmail.subject(subject)
                    .from(HaloConst.OPTIONS.get("mail_from_name"))
                    .to(to)
                    .text(content)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送模板邮件
     *
     * @param to           接收者
     * @param subject      主题
     * @param content      内容
     * @param templateName 模板路径
     */
    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName) {
        //配置邮件服务器
        HaloUtil.configMail(
                HaloConst.OPTIONS.get("mail_smtp_host"),
                HaloConst.OPTIONS.get("mail_smtp_username"),
                HaloConst.OPTIONS.get("mail_smtp_password"));
        String text = "";
        try {
            Template template = freeMarker.getConfiguration().getTemplate(templateName);
            text = FreeMarkerTemplateUtils.processTemplateIntoString(template, content);
            OhMyEmail.subject(subject)
                    .from(HaloConst.OPTIONS.get("mail_from_name"))
                    .to(to)
                    .html(text)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
