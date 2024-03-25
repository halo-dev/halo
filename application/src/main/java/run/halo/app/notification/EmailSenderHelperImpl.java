package run.halo.app.notification;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;

/**
 * <p>A default implementation of {@link EmailSenderHelper}.</p>
 *
 * @author guqing
 * @since 2.14.0
 */
@Slf4j
@Component
public class EmailSenderHelperImpl implements EmailSenderHelper {

    @Override
    @NonNull
    public JavaMailSender createJavaMailSender(EmailSenderConfig senderConfig) {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(senderConfig.getHost());
        javaMailSender.setPort(senderConfig.getPort());
        javaMailSender.setUsername(senderConfig.getUsername());
        javaMailSender.setPassword(senderConfig.getPassword());

        Properties props = javaMailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        if ("SSL".equals(senderConfig.getEncryption())) {
            props.put("mail.smtp.ssl.enable", "true");
        }

        if ("TLS".equals(senderConfig.getEncryption())) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        if ("NONE".equals(senderConfig.getEncryption())) {
            props.put("mail.smtp.ssl.enable", "false");
            props.put("mail.smtp.starttls.enable", "false");
        }

        if (log.isDebugEnabled()) {
            props.put("mail.debug", "true");
        }
        return javaMailSender;
    }

    @Override
    @NonNull
    public MimeMessagePreparator createMimeMessagePreparator(EmailSenderConfig senderConfig,
        String toEmail, String subject, String raw, String html) {
        return mimeMessage -> {
            MimeMessageHelper helper =
                new MimeMessageHelper(mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(senderConfig.getSender(), senderConfig.getDisplayName());

            helper.setSubject(subject);
            helper.setText(raw, html);
            helper.setTo(toEmail);
        };
    }
}
