package run.halo.app.notification;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;

public interface EmailSenderHelper {

    JavaMailSender createJavaMailSender(EmailSenderConfig senderConfig);

    MimeMessagePreparator createMimeMessagePreparator(EmailSenderConfig senderConfig,
        String toEmail, String subject, String raw, String html);

    @Data
    class EmailSenderConfig {
        private boolean enable;
        private String displayName;
        private String username;
        private String sender;
        private String password;
        private String host;
        private Integer port;
        private String encryption;

        /**
         * Gets email display name.
         *
         * @return display name if not blank, otherwise username.
         */
        public String getDisplayName() {
            return StringUtils.defaultIfBlank(displayName, username);
        }

        /**
         * Gets email sender address.
         *
         * @return sender if not blank, otherwise username
         */
        public String getSender() {
            return StringUtils.defaultIfBlank(sender, username);
        }
    }
}
