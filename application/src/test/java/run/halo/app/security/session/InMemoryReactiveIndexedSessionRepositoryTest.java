package run.halo.app.security.session;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.session.ReactiveFindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

/**
 * Tests for {@link InMemoryReactiveIndexedSessionRepository}.
 *
 * @author guqing
 * @since 2.15.0
 */
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
                session.setAttribute(PRINCIPAL_NAME_INDEX_NAME,
                    "test");
            })
            .map(session -> sessionRepository.indexResolver.resolveIndexesFor(session))
            .as(StepVerifier::create)
            .consumeNextWith(map -> {
                assertThat(map).containsEntry(
                    PRINCIPAL_NAME_INDEX_NAME,
                    "test");
            });

        sessionRepository.findByPrincipalName("test")
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();

        sessionRepository.findByIndexNameAndIndexValue(
                PRINCIPAL_NAME_INDEX_NAME, "test")
            .as(StepVerifier::create)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void saveTest() {
        var indexKey = createSession("fake-session-1", "test");

        assertThat(sessionRepository.getSessionIdIndexMap()).hasSize(1);
        assertThat(
            sessionRepository.getSessionIdIndexMap().containsValue(Set.of(indexKey))).isTrue();

        assertThat(sessionRepository.getIndexSessionIdMap()).hasSize(1);
        assertThat(sessionRepository.getIndexSessionIdMap().containsKey(indexKey)).isTrue();
        assertThat(sessionRepository.getIndexSessionIdMap().get(indexKey)).isEqualTo(
            Set.of("fake-session-1"));
    }

    @Test
    void saveToUpdateTest() {
        // same session id will update the index
        createSession("fake-session-1", "test");
        var indexKey2 = createSession("fake-session-1", "test2");

        assertThat(sessionRepository.getSessionIdIndexMap()).hasSize(1);
        assertThat(
            sessionRepository.getSessionIdIndexMap().containsValue(Set.of(indexKey2))).isTrue();

        assertThat(sessionRepository.getIndexSessionIdMap()).hasSize(1);
        assertThat(sessionRepository.getIndexSessionIdMap().containsKey(indexKey2)).isTrue();
        assertThat(sessionRepository.getIndexSessionIdMap().get(indexKey2)).isEqualTo(
            Set.of("fake-session-1"));
    }

    @Test
    void deleteByIdTest() {
        createSession("fake-session-2", "test1");
        sessionRepository.deleteById("fake-session-2")
            .as(StepVerifier::create)
            .verifyComplete();
        assertThat(sessionRepository.getSessionIdIndexMap()).isEmpty();
        assertThat(sessionRepository.getIndexSessionIdMap()).isEmpty();
    }

    InMemoryReactiveIndexedSessionRepository.IndexKey createSession(String sessionId,
        String principalName) {
        var indexKey = new InMemoryReactiveIndexedSessionRepository.IndexKey(
            PRINCIPAL_NAME_INDEX_NAME, principalName);
        sessionRepository.createSession()
            .doOnNext(session -> {
                session.setAttribute(indexKey.attributeName(), indexKey.attributeValue());
                session.setId(sessionId);
            })
            .flatMap(sessionRepository::save)
            .as(StepVerifier::create)
            .verifyComplete();
        return indexKey;
    }
}
