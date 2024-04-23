package run.halo.app.security.session;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import run.halo.app.event.user.PasswordChangedEvent;

@Component
@RequiredArgsConstructor
public class SessionInvalidationListener {

    private final ReactiveFindByIndexNameSessionRepository<? extends Session>
        indexedSessionRepository;
    private final ReactiveSessionRepository<? extends Session> sessionRepository;

    @Async
    @EventListener
    public void onPasswordChanged(PasswordChangedEvent event) {
        String username = event.getUsername();
        // Invalidate session
        invalidateUserSessions(username);
    }

    private void invalidateUserSessions(String username) {
        indexedSessionRepository.findByPrincipalName(username)
            .map(Map::keySet)
            .flatMapMany(Flux::fromIterable)
            .flatMap(sessionRepository::deleteById)
            .then()
            .block();
    }
}
