package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class OAuth2TemplateRenderTest {

    private TemplateEngine engine() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        engine.setMessageResolver(new StandardMessageResolver());
        engine.setLinkBuilder(new ILinkBuilder() {
            @Override
            public String getName() {
                return "test";
            }

            @Override
            public Integer getOrder() {
                return 0;
            }

            @Override
            public String buildLink(IExpressionContext context, String base, Map<String, Object> params) {
                return base;
            }
        });
        return engine;
    }

    private Context baseContext() {
        var ctx = new Context(Locale.CHINESE);
        ctx.setVariable("site", Map.of("title", "Halo", "version", "1.0", "favicon", ""));
        ctx.setVariable("publicKey", "");
        ctx.setVariable("fragmentTemplateName", "");
        return ctx;
    }

    @Test
    void renderChoicePage() {
        var ctx = baseContext();
        ctx.setVariable("displayName", "John Niang");
        ctx.setVariable("email", "john@example.com");
        ctx.setVariable("registrationId", "github");
        ctx.setVariable("allowRegistration", true);
        var html = engine().process("oauth2_choice", ctx);
        assertThat(html).contains("绑定或注册");
        assertThat(html).contains("去绑定");
        assertThat(html).contains("去注册");
        assertThat(html).contains("使用其他方式登录");
    }

    @Test
    void renderChoicePageWithRegistrationDisabled() {
        var ctx = baseContext();
        ctx.setVariable("displayName", "John Niang");
        ctx.setVariable("email", "john@example.com");
        ctx.setVariable("registrationId", "github");
        ctx.setVariable("allowRegistration", false);
        var html = engine().process("oauth2_choice", ctx);
        assertThat(html).contains("disabled");
        assertThat(html).contains("管理员已关闭注册");
    }
}
