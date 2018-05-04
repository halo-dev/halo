package cc.ryanc.halo.service;

import java.util.Map;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * @date : 2018/1/23
 */
public interface MailService {

    /**
     * 发送邮件
     *
     * @param to      接收者
     * @param subject 主题
     * @param content 内容
     */
    void sendMail(String to, String subject, String content);

    /**
     * 发送模板邮件
     *
     * @param to           接收者
     * @param subject      主题
     * @param content      内容
     * @param templateName 模板路径
     */
    void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName);
}
