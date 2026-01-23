package run.halo.app.infra.config;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.session.security.SpringSessionBackedReactiveSessionRegistry;
import run.halo.app.security.session.InMemoryReactiveIndexedSessionRepository;
import run.halo.app.security.session.ReactiveIndexedSessionRepository;

/**
 * Configuration for Spring Web Session.
 *
 * @param <S> the type of Session
 * @author johnniang
 * @since 2.22.11
 */
@Configuration
@EnableSpringWebSession
class SessionConfiguration<S extends Session> {

    @Bean
    SpringSessionBackedReactiveSessionRegistry<S> reactiveSessionRegistry(
        ReactiveSessionRepository<S> sessionRepository,
        ReactiveFindByIndexNameSessionRepository<S> indexedSessionRepository
    ) {
        return new SpringSessionBackedReactiveSessionRegistry<>(
            sessionRepository, indexedSessionRepository
        );
    }

    @Configuration
    @ConditionalOnProperty(
        value = "halo.session.store-type", havingValue = "in-memory", matchIfMissing = true
    )
    static class InMemorySessionConfig {

        @Bean
        ReactiveIndexedSessionRepository<? extends Session> inMemorySessionRepository(
            SessionProperties sessionProperties, ServerProperties serverProperties
        ) {
            var repository =
                new InMemoryReactiveIndexedSessionRepository(new ConcurrentHashMap<>());
            var timeout = sessionProperties.determineTimeout(
                () -> serverProperties.getReactive().getSession().getTimeout());
            repository.setDefaultMaxInactiveInterval(timeout);
            return repository;
        }

    }
}
