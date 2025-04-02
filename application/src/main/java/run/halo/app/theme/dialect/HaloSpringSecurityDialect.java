package run.halo.app.theme.dialect;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.thymeleaf.extras.springsecurity6.dialect.processor.AuthorizeAttrProcessor.ATTR_NAME;
import static org.thymeleaf.extras.springsecurity6.dialect.processor.AuthorizeAttrProcessor.ATTR_PRECEDENCE;
import static run.halo.app.infra.AnonymousUserConst.PRINCIPAL;
import static run.halo.app.infra.AnonymousUserConst.Role;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.util.MethodInvocationUtils;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.AttributeName;
import org.thymeleaf.extras.springsecurity6.auth.AuthUtils;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.extras.springsecurity6.util.SpringSecurityContextUtils;
import org.thymeleaf.extras.springsecurity6.util.SpringVersionSpecificUtils;
import org.thymeleaf.extras.springsecurity6.util.SpringVersionUtils;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.processor.AbstractStandardConditionalVisibilityTagProcessor;
import org.thymeleaf.templatemode.TemplateMode;
import run.halo.app.security.authorization.AuthorityUtils;

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

    private final ObjectProvider<MethodSecurityExpressionHandler> expressionHandler;

    public HaloSpringSecurityDialect(ServerSecurityContextRepository securityContextRepository,
        ObjectProvider<MethodSecurityExpressionHandler> expressionHandler) {
        this.securityContextRepository = securityContextRepository;
        this.expressionHandler = expressionHandler;
    }

    @Override
    public void afterPropertiesSet() {
        if (!SpringVersionUtils.isSpringWebFluxPresent()) {
            return;
        }

        // We have to build an anonymous authentication token here because the token won't be saved
        // into repository during anonymous authentication.
        var anonymousAuthentication =
            new AnonymousAuthenticationToken(
                "fallback", PRINCIPAL, createAuthorityList(AuthorityUtils.ROLE_PREFIX + Role)
            );
        var anonymousSecurityContext = new SecurityContextImpl(anonymousAuthentication);

        final Function<ServerWebExchange, Object> secCtxInitializer =
            exchange -> securityContextRepository.load(exchange)
                .defaultIfEmpty(anonymousSecurityContext);

        // Just overwrite the value of the attribute
        getExecutionAttributes().put(SECURITY_CONTEXT_EXECUTION_ATTRIBUTE_NAME, secCtxInitializer);
    }

    @Override
    public Set<IProcessor> getProcessors(String dialectPrefix) {
        LinkedHashSet<IProcessor> processors = new LinkedHashSet<>();
        processors.add(
            new HaloAuthorizeAttrProcessor(TemplateMode.HTML, dialectPrefix, ATTR_NAME)
        );
        processors.addAll(super.getProcessors(dialectPrefix));
        return processors;
    }

    public class HaloAuthorizeAttrProcessor
        extends AbstractStandardConditionalVisibilityTagProcessor {

        protected HaloAuthorizeAttrProcessor(TemplateMode templateMode, String dialectPrefix,
            String attrName) {
            super(templateMode, dialectPrefix, attrName, ATTR_PRECEDENCE - 10);
        }

        @Override
        protected boolean isVisible(ITemplateContext context, IProcessableElementTag tag,
            AttributeName attributeName, String attributeValue) {

            final String attrValue = (attributeValue == null ? null : attributeValue.trim());

            if (attrValue == null || attrValue.isEmpty()) {
                return false;
            }

            final Authentication authentication = AuthUtils.getAuthenticationObject(context);

            if (authentication == null) {
                return false;
            }

            // resolve expr
            var expr = Optional.of(attributeValue)
                .filter(v -> v.startsWith("${") && v.endsWith("}"))
                .map(v -> v.substring(2, v.length() - 1))
                .orElse(attributeValue);

            var expressionHandler = HaloSpringSecurityDialect.this.expressionHandler.getIfUnique();
            if (expressionHandler == null) {
                // no expression handler found
                return false;
            }

            var expression = expressionHandler.getExpressionParser().parseExpression(expr);

            var methodInvocation = MethodInvocationUtils.createFromClass(this,
                HaloAuthorizeAttrProcessor.class,
                "dummyAuthorize",
                new Class[] {Authentication.class},
                new Object[] {authentication}
            );
            var evaluationContext =
                expressionHandler.createEvaluationContext(authentication, methodInvocation);

            var expressionObjects = context.getExpressionObjects();
            var wrappedEvolutionContext = SpringVersionSpecificUtils.wrapEvaluationContext(
                evaluationContext, expressionObjects
            );

            return ExpressionUtils.evaluateAsBoolean(expression, wrappedEvolutionContext);
        }

        /**
         * This method is only used to create a method invocation for the expression parser.
         *
         * @param authentication authentication object
         * @return result of authorization expression evaluation
         */
        public Boolean dummyAuthorize(Authentication authentication) {
            throw new UnsupportedOperationException("Should not be called");
        }

    }
}
