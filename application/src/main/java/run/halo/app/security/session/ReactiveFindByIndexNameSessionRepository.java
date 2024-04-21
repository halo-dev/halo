package run.halo.app.security.session;

import java.util.Map;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.PrincipalNameIndexResolver;
import org.springframework.session.Session;
import reactor.core.publisher.Mono;

/**
 * TODO: Wait for <code>spring session 3.3 released</code> and then remove this interface.
 * if you want to remove this interface, please modify the {@link PrincipalNameIndexResolver} in
 * {@link InMemoryReactiveIndexedSessionRepository} to have a parameterized constructor(since
 * spring session 3.3) and overload the findByPrincipalName method.
 */
public interface ReactiveFindByIndexNameSessionRepository<S extends Session> {

    String PRINCIPAL_NAME_INDEX_NAME = FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME;

    Mono<Map<String, S>> findByIndexNameAndIndexValue(String indexName, String indexValue);

    default Mono<Map<String, S>> findByPrincipalName(String principalName) {
        return findByIndexNameAndIndexValue(PRINCIPAL_NAME_INDEX_NAME, principalName);
    }
}
