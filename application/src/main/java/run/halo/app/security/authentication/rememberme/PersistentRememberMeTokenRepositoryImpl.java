package run.halo.app.security.authentication.rememberme;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.lang.NonNull;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperatorImpl;

/**
 * Extension based persistent remember me token repository implementation.
 *
 * @see RememberMeToken
 */
@Component
@RequiredArgsConstructor
public class PersistentRememberMeTokenRepositoryImpl
    implements PersistentRememberMeTokenRepository {
    private final ReactiveExtensionClient client;
    private final ReactiveExtensionPaginatedOperatorImpl paginatedOperator;

    @Override
    public Mono<Void> createNewToken(PersistentRememberMeToken token) {
        var rememberMeToken = new RememberMeToken();
        var metadata = new Metadata();
        rememberMeToken.setMetadata(metadata);
        metadata.setGenerateName("token-");
        var creationTime = Instant.ofEpochMilli(token.getDate().getTime());
        metadata.setCreationTimestamp(creationTime);

        rememberMeToken.setSpec(new RememberMeToken.Spec());
        rememberMeToken.getSpec()
            .setUsername(token.getUsername())
            .setSeries(token.getSeries())
            .setTokenValue(token.getTokenValue());
        return client.create(rememberMeToken).then();
    }

    @Override
    public Mono<Void> updateToken(String series, String tokenValue, Instant lastUsed) {
        return Mono.defer(() -> getTokenExtensionForSeries(series)
                .flatMap(token -> {
                    token.getSpec().setTokenValue(tokenValue)
                        .setLastUsed(lastUsed);
                    return client.update(token);
                })
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance))
            .then();
    }

    @Override
    public Mono<PersistentRememberMeToken> getTokenForSeries(String seriesId) {
        return getTokenExtensionForSeries(seriesId)
            .map(token -> new PersistentRememberMeToken(
                token.getSpec().getUsername(),
                token.getSpec().getSeries(),
                token.getSpec().getTokenValue(),
                new Date(token.getMetadata().getCreationTimestamp().toEpochMilli())
            ));
    }

    @Override
    public Mono<Void> removeUserTokens(String username) {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(equal("spec.username", username)));
        return paginatedOperator.deleteInitialBatch(RememberMeToken.class, listOptions).then();
    }

    @Override
    public Mono<Void> removeToken(@NonNull String series) {
        return getTokenExtensionForSeries(series)
            .flatMap(client::delete)
            .then();
    }

    private Mono<RememberMeToken> getTokenExtensionForSeries(String seriesId) {
        var listOptions = ListOptions.builder()
            .fieldQuery(and(equal("spec.series", seriesId),
                isNull("metadata.deletionTimestamp")
            ))
            .build();
        return client.listBy(RememberMeToken.class, listOptions, PageRequestImpl.ofSize(1))
            .flatMap(result -> Mono.justOrEmpty(ListResult.first(result)));
    }
}
