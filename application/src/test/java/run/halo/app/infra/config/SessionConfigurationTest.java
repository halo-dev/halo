package run.halo.app.infra.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.boot.session.autoconfigure.SessionProperties;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import run.halo.app.security.session.ReactiveIndexedSessionRepository;

class SessionConfigurationTest {

    @Test
    void shouldLoadContextIfNoStoreTypeProvided() {
        var contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(SessionConfiguration.class)
            .withBean(SessionProperties.class)
            .withBean(ServerProperties.class);
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.isActive());
            assertInstanceOf(
                ReactiveIndexedSessionRepository.class,
                context.getBean(ReactiveIndexedSessionRepository.class)
            );
            assertInstanceOf(
                ReactiveIndexedSessionRepository.class,
                context.getBean(ReactiveFindByIndexNameSessionRepository.class)
            );
        });
    }

    @Test
    void shouldLoadContextIfStoreTypeIsInMemory() {
        var contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(SessionConfiguration.class)
            .withBean(SessionProperties.class)
            .withBean(ServerProperties.class)
            .withPropertyValues("halo.session.store-type=in-memory");
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            assertTrue(context.isActive());
            assertInstanceOf(
                ReactiveIndexedSessionRepository.class,
                context.getBean(ReactiveIndexedSessionRepository.class)
            );
            assertInstanceOf(
                ReactiveIndexedSessionRepository.class,
                context.getBean(ReactiveFindByIndexNameSessionRepository.class)
            );
        });
    }

    @Test
    void shouldFailToLoadContextIfStoreTypeIsInvalid() {
        var contextRunner = new ReactiveWebApplicationContextRunner()
            .withUserConfiguration(SessionConfiguration.class)
            .withBean(SessionProperties.class)
            .withBean(ServerProperties.class)
            .withPropertyValues("halo.session.store-type=invalid-type");
        contextRunner.run(context ->
            assertInstanceOf(UnsatisfiedDependencyException.class, context.getStartupFailure())
        );
    }

}
