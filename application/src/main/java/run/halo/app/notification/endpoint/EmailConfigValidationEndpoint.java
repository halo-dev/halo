package run.halo.app.notification.endpoint;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.requestbody.Builder.requestBodyBuilder;

import io.swagger.v3.oas.annotations.media.Schema;
import java.security.Principal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.mail.MailException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.User;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.notification.EmailSenderHelper;

/**
 * Validation endpoint for email config.
 *
 * @author guqing
 * @since 2.14.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConfigValidationEndpoint implements CustomEndpoint {
    private static final String EMAIL_SUBJECT = "测试邮件 - 验证邮箱连通性";
    private static final String EMAIL_BODY = """
        你好！<br/>
        这是一封测试邮件，旨在验证您的邮箱发件配置是否正确。<br/>
        此邮件由系统自动发送，请勿回复。<br/>
        祝好
        """;

    private final EmailSenderHelper emailSenderHelper;
    private final ReactiveExtensionClient client;

    @Override
    public RouterFunction<ServerResponse> endpoint() {
        var tag = "console.api.notification.halo.run/v1alpha1/Notifier";
        return SpringdocRouteBuilder.route()
            .POST("/notifiers/default-email-notifier/verify-connection",
                this::verifyEmailSenderConfig,
                builder -> builder.operationId("VerifyEmailSenderConfig")
                    .description("Verify email sender config.")
                    .tag(tag)
                    .requestBody(requestBodyBuilder()
                        .required(true)
                        .implementation(ValidationRequest.class)
                    )
                    .response(responseBuilder().implementation(Void.class))
            )
            .build();
    }

    private Mono<ServerResponse> verifyEmailSenderConfig(ServerRequest request) {
        return request.bodyToMono(ValidationRequest.class)
            .switchIfEmpty(
                Mono.error(new ServerWebInputException("Required request body is missing."))
            )
            .flatMap(validationRequest -> getCurrentUserEmail()
                .flatMap(recipient -> {
                    var mailSender = emailSenderHelper.createJavaMailSender(validationRequest);
                    var message = emailSenderHelper.createMimeMessagePreparator(validationRequest,
                        recipient, EMAIL_SUBJECT, EMAIL_BODY, EMAIL_BODY);
                    try {
                        mailSender.send(message);
                    } catch (MailException e) {
                        String errorMsg =
                            "Failed to send email, please check your email configuration.";
                        log.error(errorMsg, e);
                        throw new ServerWebInputException(errorMsg, null, e);
                    }
                    return ServerResponse.ok().build();
                })
            );
    }

    Mono<String> getCurrentUserEmail() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Principal::getName)
            .flatMap(username -> client.fetch(User.class, username))
            .flatMap(user -> {
                var email = user.getSpec().getEmail();
                if (StringUtils.isBlank(email)) {
                    return Mono.error(new ServerWebInputException(
                        "Your email is missing, please set it in your profile."));
                }
                return Mono.just(email);
            });
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(name = "EmailConfigValidationRequest")
    static class ValidationRequest extends EmailSenderHelper.EmailSenderConfig {
    }

    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("console.api.notification.halo.run/v1alpha1");
    }
}
