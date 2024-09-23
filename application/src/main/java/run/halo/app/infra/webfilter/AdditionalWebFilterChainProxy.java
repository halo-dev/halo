package run.halo.app.infra.webfilter;

import lombok.Setter;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.extensionpoint.ExtensionGetter;
import run.halo.app.security.AdditionalWebFilter;

public class AdditionalWebFilterChainProxy implements WebFilter {

    private final ExtensionGetter extensionGetter;

    @Setter
    private WebFilterChainProxy.WebFilterChainDecorator filterChainDecorator;

    public AdditionalWebFilterChainProxy(ExtensionGetter extensionGetter) {
        this.extensionGetter = extensionGetter;
        this.filterChainDecorator = new WebFilterChainProxy.DefaultWebFilterChainDecorator();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return extensionGetter.getEnabledExtensions(AdditionalWebFilter.class)
            .sort(AnnotationAwareOrderComparator.INSTANCE)
            .cast(WebFilter.class)
            .collectList()
            .map(filters -> filterChainDecorator.decorate(chain, filters))
            .flatMap(decoratedChain -> decoratedChain.filter(exchange));
    }

}
