package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.service.MailService;
import io.github.biezhi.ome.OhMyEmail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static org.junit.Assert.*;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/3/18
 */
public class MailServiceImplTest {

    @Before
    public void before() throws GeneralSecurityException {
        // 配置，一次即可
        OhMyEmail.config(OhMyEmail.SMTP_163(false), "ryan0up@163.com", "wangdashen666");
    }

    @Test
    public void testSendText() throws MessagingException {
        OhMyEmail.subject("这是一封测试TEXT邮件")
                .from("RYAN0UP")
                .to("709831589@qq.com")
                .text("信件内容")
                .send();
    }

    @Test
    public void testSendHtml() throws MessagingException {
        OhMyEmail.subject("这是一封测试HTML邮件")
                .from("王爵的QQ邮箱")
                .to("921293209@qq.com")
                .html("<h1 font=red>信件内容</h1>")
                .send();
    }

    @Test
    public void testSendAttach() throws MessagingException {
        OhMyEmail.subject("这是一封测试附件邮件")
                .from("王爵的QQ邮箱")
                .to("921293209@qq.com")
                .html("<h1 font=red>信件内容</h1>")
                .attach(new File("/Users/biezhi/Downloads/hello.jpeg"), "测试图片.jpeg")
                .send();
    }
}