package run.halo.app.notification;

import reactor.core.publisher.Flux;
import run.halo.app.core.extension.notification.Reason;

public interface RecipientResolver {

    Flux<Subscriber> resolve(Reason reason);
}
