package run.halo.app.security.session;

import org.springframework.session.ReactiveFindByIndexNameSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;

public interface ReactiveIndexedSessionRepository<S extends Session>
    extends ReactiveSessionRepository<S>, ReactiveFindByIndexNameSessionRepository<S> {
}
