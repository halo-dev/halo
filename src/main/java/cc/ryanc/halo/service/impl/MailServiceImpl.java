package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.model.dto.HaloConst;
import cc.ryanc.halo.service.MailService;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/23
 */
@Service
public class MailServiceImpl implements MailService{

    private static final String HOST = HaloConst.OPTIONS.get("mail_smtp_host");
    private static final String USERNAME = HaloConst.OPTIONS.get("mail_smtp_username");
    private static final String PASSWORD = HaloConst.OPTIONS.get("mail_smtp_password");
    private static final String FROM_EMAIL = HaloConst.OPTIONS.get("mail_smtp_username");
    private static final String FROM_NAME = HaloConst.OPTIONS.get("mail_from_name");
    private static final JavaMailSenderImpl mailSender = createMailSender();

    @Autowired
    private FreeMarkerConfigurer freeMarker;

    /**
     * 配置邮件发送器
     * @return JavaMailSenderImpl
     */
    private static JavaMailSenderImpl createMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(HOST);
        sender.setUsername(USERNAME);
        sender.setPassword(PASSWORD);
        sender.setDefaultEncoding("utf-8");
        Properties p = new Properties();
        p.setProperty("mail.smtp.auth", "true");
        p.setProperty("mail.smtp.timeout", "25000");
        p.setProperty("mail.smtp.starttls.enable","true");
        p.setProperty("mail.smtp.starttls.required","true");
        sender.setJavaMailProperties(p);
        return sender;
    }

    /**
     * 发送邮件
     * @param to to
     * @param subject subject
     * @param content content
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"UTF-8");
            helper.setFrom(FROM_EMAIL,FROM_NAME);
            helper.setTo(to);
            helper.setSubject(subject);

            Template template = freeMarker.getConfiguration().getTemplate("common/mail.ftl");
            Map<String,String> map = new HashMap<String,String>();
            map.put("hahah","哈哈哈");
            map.put("hehehe","呵呵呵");
            content = FreeMarkerTemplateUtils.processTemplateIntoString(template,map);
            helper.setText(content,true);
            mailSender.send(mimeMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
