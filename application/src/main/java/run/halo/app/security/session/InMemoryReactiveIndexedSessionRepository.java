package run.halo.app.security.session;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.session.DelegatingIndexResolver;
import org.springframework.session.IndexResolver;
import org.springframework.session.MapSession;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.Session;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InMemoryReactiveIndexedSessionRepository extends ReactiveMapSessionRepository
    implements ReactiveIndexedSessionRepository<MapSession>, DisposableBean {

    final IndexResolver<MapSession> indexResolver =
        new DelegatingIndexResolver<>(new PrincipalNameIndexResolver<>(PRINCIPAL_NAME_INDEX_NAME));

    private final ConcurrentMap<String, Set<IndexKey>> sessionIdIndexMap =
        new ConcurrentHashMap<>();
    private final ConcurrentMap<IndexKey, Set<String>> indexSessionIdMap =
        new ConcurrentHashMap<>();

    /**
     * Prevent other requests from being parsed and acquiring the session during its deletion,
     * which could result in an unintended renewal. Currently, it acts as a buffer, and having a
     * slightly prolonged expiration period is sufficient.
     */
    private final Cache<String, Boolean> invalidateSessionIds = CacheBuilder.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(10))
        .maximumSize(10_000)
        .build();

    public InMemoryReactiveIndexedSessionRepository(Map<String, Session> sessions) {
        super(sessions);
    }

    @Override
    public Mono<Void> save(MapSession session) {
        if (invalidateSessionIds.getIfPresent(session.getId()) != null) {
            return this.deleteById(session.getId());
        }
        return super.save(session)
            .then(updateIndex(session));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return removeIndex(id)
            .then(Mono.defer(() -> {
                invalidateSessionIds.put(id, true);
                return super.deleteById(id);
            }));
    }

    @Override
    public Mono<Map<String, MapSession>> findByIndexNameAndIndexValue(String indexName,
        String indexValue) {
        var indexKey = new IndexKey(indexName, indexValue);
        return Flux.fromStream((() -> indexSessionIdMap.getOrDefault(indexKey, Set.of()).stream()))
            .flatMap(this::findById)
            .collectMap(Session::getId);
    }

    @Override
    public Mono<Map<String, MapSession>> findByPrincipalName(String principalName) {
        return this.findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, principalName);
    }

    @Override
    public void destroy() {
        sessionIdIndexMap.clear();
        indexSessionIdMap.clear();
        invalidateSessionIds.invalidateAll();
    }

    Mono<Void> removeIndex(String sessionId) {
        return getIndexes(sessionId)
            .doOnNext(indexKey -> indexSessionIdMap.computeIfPresent(indexKey,
                (key, sessionIdSet) -> {
                    sessionIdSet.remove(sessionId);
                    return sessionIdSet.isEmpty() ? null : sessionIdSet;
                })
            )
            .then(Mono.defer(() -> {
                sessionIdIndexMap.remove(sessionId);
                return Mono.empty();
            }))
            .then();
    }

    Mono<Void> updateIndex(MapSession session) {
        return removeIndex(session.getId())
            .then(Mono.defer(() -> {
                if (!session.getId().equals(session.getOriginalId())) {
                    return removeIndex(session.getOriginalId());
                }
                return Mono.empty();
            }))
            .then(Mono.defer(() -> {
                indexResolver.resolveIndexesFor(session)
                    .forEach((name, value) -> {
                        IndexKey indexKey = new IndexKey(name, value);
                        indexSessionIdMap.computeIfAbsent(indexKey,
                                unusedSet -> ConcurrentHashMap.newKeySet())
                            .add(session.getId());
                        // Update sessionIdIndexMap
                        sessionIdIndexMap.computeIfAbsent(session.getId(),
                                unusedSet -> ConcurrentHashMap.newKeySet())
                            .add(indexKey);
                    });
                return Mono.empty();
            }))
            .then();
    }

    Flux<IndexKey> getIndexes(String sessionId) {
        return Flux.fromIterable(sessionIdIndexMap.getOrDefault(sessionId, Set.of()));
    }

    /**
     * For testing purpose.
     */
    ConcurrentMap<String, Set<IndexKey>> getSessionIdIndexMap() {
        return sessionIdIndexMap;
    }

    /**
     * For testing purpose.
     */
    ConcurrentMap<IndexKey, Set<String>> getIndexSessionIdMap() {
        return indexSessionIdMap;
    }

    record IndexKey(String attributeName, String attributeValue) {
    }
}
