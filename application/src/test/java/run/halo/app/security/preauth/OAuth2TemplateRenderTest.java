package run.halo.app.security.preauth;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IExpressionContext;
import org.thymeleaf.linkbuilder.ILinkBuilder;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

class OAuth2TemplateRenderTest {

    private static final Pattern MESSAGE_KEY_PATTERN = Pattern.compile("#\\{([^}]+)\\}");

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
        assertThat(html).contains("href=\"/login/oauth2/register\"");
        assertThat(html).doesNotContain("aria-disabled=\"true\"");
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
        assertThat(html).doesNotContain("href=\"/login/oauth2/register\"");
        assertThat(html).contains("aria-disabled=\"true\"");
        assertThat(html).contains("tabindex=\"-1\"");
    }

    @Test
    void allMessageKeysResolveInEveryLocaleBundle() throws IOException {
        for (var templateName : Set.of("oauth2_choice", "oauth2_register")) {
            var keys = new TreeSet<>(extractMessageKeys(templateName));
            if ("oauth2_register".equals(templateName)) {
                // Field error codes referenced from PreAuthOAuth2Endpoint but not present in the template text.
                keys.add("oauth2.register.error.duplicate-username");
                keys.add("oauth2.register.error.email-already-taken");
                keys.add("oauth2.register.error.agreed-to-terms.required");
            }
            for (var localeSuffix : Set.of("", "_en", "_es", "_zh_TW")) {
                var bundle = loadBundle(templateName + localeSuffix);
                for (var key : keys) {
                    assertThat(bundle)
                            .as("%s%s must define message key %s", templateName, localeSuffix, key)
                            .containsKey(key);
                }
            }
        }
    }

    private static Set<String> extractMessageKeys(String templateName) throws IOException {
        var template = loadResource("templates/" + templateName + ".html");
        var matcher = MESSAGE_KEY_PATTERN.matcher(template);
        var keys = new TreeSet<String>();
        while (matcher.find()) {
            keys.add(matcher.group(1));
        }
        return keys;
    }

    private static Properties loadBundle(String bundleName) throws IOException {
        var properties = new Properties();
        try (InputStream in = loadResourceAsStream("templates/" + bundleName + ".properties")) {
            assertThat(in).as("bundle %s must exist", bundleName).isNotNull();
            properties.load(in);
        }
        return properties;
    }

    private static String loadResource(String path) throws IOException {
        try (InputStream in = loadResourceAsStream(path)) {
            assertThat(in).as("resource %s must exist", path).isNotNull();
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static InputStream loadResourceAsStream(String path) {
        return OAuth2TemplateRenderTest.class.getClassLoader().getResourceAsStream(path);
    }
}
