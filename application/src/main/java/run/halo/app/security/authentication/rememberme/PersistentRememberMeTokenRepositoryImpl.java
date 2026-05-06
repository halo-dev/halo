package run.halo.app.security.authentication.rememberme;

import static run.halo.app.extension.ExtensionUtil.defaultSort;
import static run.halo.app.extension.index.query.Queries.equal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.ReactiveExtensionPaginatedOperatorImpl;

/**
 * Extension based persistent remember me token repository implementation.
 *
 * @see RememberMeToken
 */
@Component
@RequiredArgsConstructor
class PersistentRememberMeTokenRepositoryImpl implements PersistentRememberMeTokenRepository {
    private final ReactiveExtensionClient client;
    private final ReactiveExtensionPaginatedOperatorImpl paginatedOperator;

    @Override
    public Mono<RememberMeToken> createNewToken(RememberMeToken token) {
        return client.create(token);
    }

    @Override
    public Mono<RememberMeToken> updateToken(RememberMeToken token) {
        return client.update(token);
    }

    @Override
    public Mono<RememberMeToken> getTokenForSeries(String seriesId) {
        return getTokenExtensionForSeries(seriesId);
    }

    @Override
    public Mono<Void> removeUserTokens(String username) {
        var listOptions = ListOptions.builder().andQuery(equal("spec.username", username)).build();
        return paginatedOperator.deleteInitialBatch(RememberMeToken.class, listOptions).then();
    }

    @Override
    public Mono<Void> removeToken(String series) {
        return getTokenExtensionForSeries(series).flatMap(client::delete).then();
    }

    private Mono<RememberMeToken> getTokenExtensionForSeries(String seriesId) {
        var listOptions =
                ListOptions.builder()
                        .andQuery(equal("spec.series", seriesId))
                        .andQuery(ExtensionUtil.notDeleting())
                        .build();
        return client.listTopNames(RememberMeToken.class, listOptions, defaultSort(), 1)
                .next()
                .flatMap(name -> client.fetch(RememberMeToken.class, name));
    }
}
