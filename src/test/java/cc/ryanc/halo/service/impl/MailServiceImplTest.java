package cc.ryanc.halo.service.impl;

import cc.ryanc.halo.service.MailService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.Assert.*;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/3/18
 */
public class MailServiceImplTest {

    @Autowired
    private MailService mailService;

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void testMail(){
        mailService.sendMail("i@ryanc.cc","邮件测试","邮件测试");
    }

    @Test
    public void sendSimpleMail() throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ryan0up@163.com");
        message.setTo("i@ryanc.cc");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");

        mailSender.send(message);
    }
}