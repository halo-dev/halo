package run.halo.app.service;

import java.util.Map;

/**
 * Mail service interface.
 *
 * @author ryanwang
 * @date 2019-03-17
 */
public interface MailService {

    /**
     * Send a simple email
     *
     * @param to      recipient
     * @param subject subject
     * @param content content
     */
    void sendMail(String to, String subject, String content);

    /**
     * Send a email with html
     *
     * @param to           recipient
     * @param subject      subject
     * @param content      content
     * @param templateName template name
     */
    void sendTemplateMail(String to, String subject, Map<String, Object> content, String templateName);

    /**
     * Send mail with attachments
     *
     * @param to             recipient
     * @param subject        subject
     * @param content        content
     * @param templateName   template name
     * @param attachFilename attachment path
     */
    void sendAttachMail(String to, String subject, Map<String, Object> content, String templateName, String attachFilename);
}
