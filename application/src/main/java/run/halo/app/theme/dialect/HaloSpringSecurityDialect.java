package run.halo.app.theme.dialect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;
import org.thymeleaf.extras.springsecurity6.util.SpringVersionUtils;
import reactor.core.publisher.Mono;

/**
 * HaloSpringSecurityDialect overwrites value of thymeleafSpringSecurityContext.
 *
 * @author johnniang
 */
public class HaloSpringSecurityDialect extends SpringSecurityDialect {

    private static final String SECURITY_CONTEXT_EXECUTION_ATTRIBUTE_NAME =
        "ThymeleafReactiveModelAdditions:"
            + SpringSecurityContextUtils.SECURITY_CONTEXT_MODEL_ATTRIBUTE_NAME;
    private static final String CSRF_EXECUTION_ATTRIBUTE_NAME =
        "ThymeleafReactiveModelAdditions:" + CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME;

    private final Map<String, Object> executionAttributes;

    private final ServerSecurityContextRepository securityContextRepository;

    public HaloSpringSecurityDialect(ServerSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
        executionAttributes = new HashMap<>(3, 1.0f);
        initExecutionAttributes();
    }

    private void initExecutionAttributes() {
        if (!SpringVersionUtils.isSpringWebFluxPresent()) {
            return;
        }

        final Function<ServerWebExchange, Object> secCtxInitializer =
            (exchange) -> securityContextRepository.load(exchange);

        final Function<ServerWebExchange, Object> csrfTokenInitializer =
            (exchange) -> {
                final Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
                if (csrfToken == null) {
                    return Mono.empty();
                }
                return csrfToken.doOnSuccess(
                    token -> exchange.getAttributes()
                        .put(CsrfRequestDataValueProcessor.DEFAULT_CSRF_ATTR_NAME, token));
            };

        executionAttributes.put(SECURITY_CONTEXT_EXECUTION_ATTRIBUTE_NAME, secCtxInitializer);
        executionAttributes.put(CSRF_EXECUTION_ATTRIBUTE_NAME, csrfTokenInitializer);
    }

    @Override
    public Map<String, Object> getExecutionAttributes() {
        return executionAttributes;
    }
}
