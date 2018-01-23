package cc.ryanc.halo.service;

/**
 * @author : RYAN0UP
 * @version : 1.0
 * description :
 * @date : 2018/1/23
 */
public interface MailService {
    void sendMail(String to,String subject,String content);
}
