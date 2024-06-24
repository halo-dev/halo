package run.halo.app.security.authentication.rememberme;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.router.selector.FieldSelector;

/**
 * A {@link Reconciler} for {@link RememberMeToken}.
 *
 * @author guqing
 * @since 2.17.0
 */
@Component
@RequiredArgsConstructor
public class RememberTokenReconciler implements Reconciler<Reconciler.Request> {
    private final ExtensionClient client;
    private final RememberMeCookieResolver rememberMeCookieResolver;

    @Override
    public Result reconcile(Request request) {
        client.fetch(RememberMeToken.class, request.name())
            .ifPresent(this::cleanUpExpiredTokens);
        return Result.doNotRetry();
    }

    void cleanUpExpiredTokens(RememberMeToken token) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            and(equal("spec.username", token.getSpec().getUsername()),
                isNull("metadata.deletionTimestamp"))
        ));
        client.listAll(RememberMeToken.class, listOptions,
                Sort.by("metadata.creationTimestamp").descending())
            .stream()
            .filter(this::isTokenExpired)
            .forEach(client::delete);
    }

    private boolean isTokenExpired(RememberMeToken token) {
        var creationTime = token.getMetadata().getCreationTimestamp();
        // expireTime < now
        return creationTime.plus(getTokenValidity()).isBefore(Instant.now());
    }

    private Duration getTokenValidity() {
        return rememberMeCookieResolver.getCookieMaxAge();
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new RememberMeToken())
            .syncAllOnStart(false)
            .build();
    }
}
