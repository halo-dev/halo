package run.halo.app.notification;

import java.util.Optional;
import org.springframework.util.Assert;
import run.halo.app.infra.AnonymousUserConst;

public record Subscriber(UserIdentity identity, String subscriptionName) {
    public Subscriber {
        Assert.notNull(identity, "The subscriber must not be null");
        Assert.hasText(subscriptionName, "The subscription name must not be blank");
    }

    public String name() {
        return identity.name();
    }

    public String username() {
        return identity.isAnonymous() ? AnonymousUserConst.PRINCIPAL : identity.name();
    }

    public boolean isAnonymous() {
        return identity.isAnonymous();
    }

    public Optional<String> getEmail() {
        return identity.getEmail();
    }
}
