package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.service.MailService;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.mail.internet.MimeMessage;
import java.util.Map;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/23
 */
@Service
public class MailServiceImpl implements MailService{

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private FreeMarkerConfigurer freeMarker;

    @Value("${spring.mail.username}")
    private String emailFrom;

//    /**
//     * 配置邮件发送器
//     * @return JavaMailSenderImpl
//     */
//    private static JavaMailSenderImpl createMailSender() {
//        JavaMailSenderImpl sender = new JavaMailSenderImpl();
//        sender.setHost(HOST);
//        sender.setUsername(USERNAME);
//        sender.setPassword(PASSWORD);
//        sender.setDefaultEncoding("utf-8");
//        Properties p = new Properties();
//        p.setProperty("mail.smtp.auth", "true");
//        p.setProperty("mail.smtp.timeout", "25000");
//        p.setProperty("mail.smtp.starttls.enable","true");
//        p.setProperty("mail.smtp.starttls.required","true");
//        sender.setJavaMailProperties(p);
//        return sender;
//    }

    /**
     * 发送邮件
     *
     * @param to to
     * @param subject subject
     * @param content content
     */
    @Override
    public void sendMail(String to, String subject, String content) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        //发送者
        mailMessage.setFrom(emailFrom);
        mailMessage.setTo(to);
        mailMessage.setSubject(subject);
        mailMessage.setText(content);
        mailSender.send(mailMessage);
    }

    /**
     * 发送模板邮件
     *
     * @param to 接收者
     * @param subject 主题
     * @param content 内容
     * @param templateName 模板路径
     */
    @Override
    public void sendTemplateMail(String to, String subject, Map<String, Object> content,String templateName) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String text = "";
        try{
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage,true,"utf-8");
            helper.setFrom(emailFrom);
            helper.setTo(to);
            helper.setSubject(subject);

            Template template = freeMarker.getConfiguration().getTemplate(templateName);
            text = FreeMarkerTemplateUtils.processTemplateIntoString(template,content);
            helper.setText(text,true);
            mailSender.send(mimeMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
