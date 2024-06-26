package run.halo.app.security.authentication.rememberme;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.lessThan;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.RememberMeToken;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

/**
 * A cleaner for remember me tokens that cleans up expired tokens periodically.
 *
 * @author guqing
 * @since 2.17.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RememberTokenCleaner {
    private final ReactiveExtensionPaginatedOperator paginatedOperator;
    private final RememberMeCookieResolver rememberMeCookieResolver;

    /**
     * Clean up expired tokens every day at 3:00 AM.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUpExpiredTokens() {
        paginatedOperator.deleteInitialBatch(RememberMeToken.class, getExpiredTokensListOptions())
            .then().block();
        log.info("Expired remember me tokens have been cleaned up.");
    }

    ListOptions getExpiredTokensListOptions() {
        var listOptions = new ListOptions();
        listOptions.setFieldSelector(FieldSelector.of(
            and(isNull("metadata.deletionTimestamp"),
                lessThan("metadata.creationTimestamp", getExpirationThreshold().toString())
            )
        ));
        return listOptions;
    }

    protected Instant getExpirationThreshold() {
        return Instant.now().minus(getTokenValidity());
    }

    protected Duration getTokenValidity() {
        return rememberMeCookieResolver.getCookieMaxAge();
    }
}
