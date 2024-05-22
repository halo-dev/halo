package run.halo.app.theme.dialect;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static run.halo.app.infra.AnonymousUserConst.PRINCIPAL;
import static run.halo.app.infra.AnonymousUserConst.Role;

import java.util.function.Function;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;
import org.thymeleaf.extras.springsecurity6.util.SpringVersionUtils;

/**
 * HaloSpringSecurityDialect overwrites value of thymeleafSpringSecurityContext.
 *
 * @author johnniang
 */
public class HaloSpringSecurityDialect extends SpringSecurityDialect implements InitializingBean {

    private static final String SECURITY_CONTEXT_EXECUTION_ATTRIBUTE_NAME =
        "ThymeleafReactiveModelAdditions:"
            + SpringSecurityContextUtils.SECURITY_CONTEXT_MODEL_ATTRIBUTE_NAME;

    private final ServerSecurityContextRepository securityContextRepository;

    public HaloSpringSecurityDialect(ServerSecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public void afterPropertiesSet() {
        if (!SpringVersionUtils.isSpringWebFluxPresent()) {
            return;
        }

        // We have to build an anonymous authentication token here because the token won't be saved
        // into repository during anonymous authentication.
        var anonymousAuthentication =
            new AnonymousAuthenticationToken("fallback", PRINCIPAL, createAuthorityList(Role));
        var anonymousSecurityContext = new SecurityContextImpl(anonymousAuthentication);

        final Function<ServerWebExchange, Object> secCtxInitializer =
            exchange -> securityContextRepository.load(exchange)
                .defaultIfEmpty(anonymousSecurityContext);

        // Just overwrite the value of the attribute
        getExecutionAttributes().put(SECURITY_CONTEXT_EXECUTION_ATTRIBUTE_NAME, secCtxInitializer);
    }
}
