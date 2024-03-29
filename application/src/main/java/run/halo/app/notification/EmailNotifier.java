package run.halo.app.notification;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.concurrent.atomic.AtomicReference;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.util.Pair;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.infra.utils.JsonUtils;
import run.halo.app.notification.EmailSenderHelper.EmailSenderConfig;

/**
 * <p>A notifier that can send email.</p>
 *
 * @author guqing
 * @see ReactiveNotifier
 * @see JavaMailSenderImpl
 * @since 2.10.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotifier implements ReactiveNotifier {

    private final SubscriberEmailResolver subscriberEmailResolver;
    private final NotificationTemplateRender notificationTemplateRender;
    private final EmailSenderHelper emailSenderHelper;
    private final AtomicReference<Pair<EmailSenderConfig, JavaMailSender>>
        emailSenderConfigPairRef = new AtomicReference<>();

    @Override
    public Mono<Void> notify(NotificationContext context) {
        JsonNode senderConfig = context.getSenderConfig();
        var emailSenderConfig =
            JsonUtils.DEFAULT_JSON_MAPPER.convertValue(senderConfig, EmailSenderConfig.class);

        if (!emailSenderConfig.isEnable()) {
            log.debug("Email notifier is disabled, skip sending email.");
            return Mono.empty();
        }

        JavaMailSender javaMailSender = getJavaMailSender(emailSenderConfig);

        String recipient = context.getMessage().getRecipient();
        var subscriber = new Subscription.Subscriber();
        subscriber.setName(recipient);
        var payload = context.getMessage().getPayload();
        return subscriberEmailResolver.resolve(subscriber)
            .flatMap(toEmail -> {
                if (StringUtils.isBlank(toEmail)) {
                    log.debug("Cannot resolve email for subscriber: [{}], skip sending email.",
                        subscriber);
                    return Mono.empty();
                }
                var htmlMono = appendHtmlBodyFooter(payload.getAttributes())
                    .doOnNext(footer -> {
                        if (StringUtils.isNotBlank(payload.getHtmlBody())) {
                            payload.setHtmlBody(payload.getHtmlBody() + "\n" + footer);
                        }
                    });
                var rawMono = appendRawBodyFooter(payload.getAttributes())
                    .doOnNext(footer -> {
                        if (StringUtils.isNotBlank(payload.getRawBody())) {
                            payload.setRawBody(payload.getRawBody() + "\n" + footer);
                        }
                    });
                return Mono.when(htmlMono, rawMono)
                    .thenReturn(toEmail);
            })
            .map(toEmail -> getMimeMessagePreparator(toEmail, emailSenderConfig, payload))
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(javaMailSender::send)
            .then();
    }

    @NonNull
    private MimeMessagePreparator getMimeMessagePreparator(String toEmail,
        EmailSenderConfig emailSenderConfig, NotificationContext.MessagePayload payload) {
        return emailSenderHelper.createMimeMessagePreparator(emailSenderConfig, toEmail,
            payload.getTitle(),
            payload.getRawBody(), payload.getHtmlBody());
    }

    JavaMailSender getJavaMailSender(EmailSenderConfig emailSenderConfig) {
        return emailSenderConfigPairRef.updateAndGet(pair -> {
            if (pair != null && pair.getFirst().equals(emailSenderConfig)) {
                return pair;
            }
            return Pair.of(emailSenderConfig,
                emailSenderHelper.createJavaMailSender(emailSenderConfig));
        }).getSecond();
    }

    Mono<String> appendRawBodyFooter(ReasonAttributes attributes) {
        return notificationTemplateRender.render("""
            ---
            如果您不想再收到此类通知，点击链接退订: [(${unsubscribeUrl})]
            [(${site.title})]
            """, attributes);
    }

    Mono<String> appendHtmlBodyFooter(ReasonAttributes attributes) {
        return notificationTemplateRender.render("""
            <div class="footer" style="font-size: 12px; color: #666;">
            <a th:href="${site.url}" th:text="${site.title}"></a>
            <p class="unsubscribe">
            &mdash;<br />请勿直接回复此邮件，
            <a th:href="|${site.url}/uc/notifications|">查看通知</a>
            或
            <a th:href="${unsubscribeUrl}">取消订阅</a>。
            </p>
            </div>
            """, attributes);
    }
}
