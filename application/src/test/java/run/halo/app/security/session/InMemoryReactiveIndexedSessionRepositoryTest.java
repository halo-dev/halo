package run.halo.app.security.session;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class InMemoryReactiveIndexedSessionRepositoryTest {
    private InMemoryReactiveIndexedSessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        sessionRepository = new InMemoryReactiveIndexedSessionRepository(new ConcurrentHashMap<>());
    }

    @Test
    void principalNameIndexTest() {
        sessionRepository.createSession()
            .doOnNext(session -> {
                session.setAttribute(ReactiveIndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                    "test");
            })
            .map(session -> sessionRepository.indexResolver.resolveIndexesFor(session))
            .as(StepVerifier::create)
            .consumeNextWith(map -> {
                assertThat(map).containsEntry(
                    ReactiveIndexedSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                    "test");
            });
    }
}
