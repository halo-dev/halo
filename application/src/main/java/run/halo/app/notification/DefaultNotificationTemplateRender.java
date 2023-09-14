package run.halo.app.notification;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.SystemConfigurableEnvironmentFetcher;
import run.halo.app.infra.SystemSetting;

/**
 * <p>Default implementation of {@link NotificationTemplateRender}.</p>
 * <p>This implementation use {@link TemplateEngine} to render template, and the template engine
 * use {@link StringTemplateResolver} to resolve template, so the template
 * in {@link #render(String template, Map)} is template content.</p>
 * <p>Template syntax:
 * <a href="https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#textual-syntax">usingthymeleaf.html#textual-syntax</a>
 * </p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Component
@RequiredArgsConstructor
public class DefaultNotificationTemplateRender implements NotificationTemplateRender {

    private static final TemplateEngine TEMPLATE_ENGINE = createTemplateEngine();

    private final SystemConfigurableEnvironmentFetcher environmentFetcher;
    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public Mono<String> render(String template, Map<String, Object> model) {
        var context = new Context(Locale.getDefault(), model);
        var globalAttributeMono = getBasicSetting()
            .doOnNext(basic -> {
                var site = new HashMap<>();
                site.put("title", basic.getTitle());
                site.put("logo", basic.getLogo());
                site.put("subtitle", basic.getSubtitle());
                site.put("url", externalUrlSupplier.getRaw());
                context.setVariable("site", site);
            });
        return Mono.when(globalAttributeMono)
            .then(Mono.fromSupplier(() ->
                TEMPLATE_ENGINE.process(defaultString(template), context)));
    }

    static TemplateEngine createTemplateEngine() {
        var template = new SpringTemplateEngine();
        template.setTemplateResolver(new StringTemplateResolver());
        return template;
    }

    Mono<SystemSetting.Basic> getBasicSetting() {
        return environmentFetcher.fetch(SystemSetting.Basic.GROUP, SystemSetting.Basic.class);
    }
}
