package run.halo.app.security;

import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.ANONYMOUS_AUTHENTICATION;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.AUTHENTICATION;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.FIRST;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.FORM_LOGIN;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.HTTP_BASIC;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.LAST;
import static org.springframework.security.config.web.server.SecurityWebFiltersOrder.OAUTH2_AUTHORIZATION_CODE;

import lombok.Setter;
import org.pf4j.ExtensionPoint;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.authentication.SecurityConfigurer;

@Component
// Specific an order here to control the order or security configurer initialization
@Order(100)
public class SecurityWebFiltersConfigurer implements SecurityConfigurer {

    private final ExtensionGetter extensionGetter;

    public SecurityWebFiltersConfigurer(ExtensionGetter extensionGetter) {
        this.extensionGetter = extensionGetter;
    }

    @Override
    public void configure(ServerHttpSecurity http) {
        http
            .addFilterAt(
                new SecurityWebFilterChainProxy(BeforeSecurityWebFilter.class),
                FIRST
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(HttpBasicSecurityWebFilter.class),
                HTTP_BASIC
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(FormLoginSecurityWebFilter.class),
                FORM_LOGIN
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(AuthenticationSecurityWebFilter.class),
                AUTHENTICATION
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(AnonymousAuthenticationSecurityWebFilter.class),
                ANONYMOUS_AUTHENTICATION
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(OAuth2AuthorizationCodeSecurityWebFilter.class),
                OAUTH2_AUTHORIZATION_CODE
            )
            .addFilterAt(
                new SecurityWebFilterChainProxy(AfterSecurityWebFilter.class),
                LAST
            )
        ;
    }

    public class SecurityWebFilterChainProxy implements WebFilter {

        @Setter
        private WebFilterChainProxy.WebFilterChainDecorator filterChainDecorator;

        private final Class<? extends ExtensionPoint> extensionPointClass;

        public SecurityWebFilterChainProxy(Class<? extends ExtensionPoint> extensionPointClass) {
            this.extensionPointClass = extensionPointClass;
            this.filterChainDecorator = new WebFilterChainProxy.DefaultWebFilterChainDecorator();
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            return extensionGetter.getExtensions(this.extensionPointClass)
                .sort(AnnotationAwareOrderComparator.INSTANCE)
                .cast(WebFilter.class)
                .collectList()
                .map(filters -> filterChainDecorator.decorate(chain, filters))
                .flatMap(decoratedChain -> decoratedChain.filter(exchange));
        }
    }

}
