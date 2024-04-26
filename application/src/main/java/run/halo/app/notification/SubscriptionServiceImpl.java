package run.halo.app.notification;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;
import static run.halo.app.extension.index.query.QueryFactory.startsWith;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import run.halo.app.core.extension.notification.Subscription;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.infra.ReactiveExtensionPaginatedOperator;

@Component
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {
    private final ReactiveExtensionClient client;
    private final ReactiveExtensionPaginatedOperator paginatedOperator;

    @Override
    public Mono<Void> remove(Subscription.Subscriber subscriber,
        Subscription.InterestReason interestReason) {
        Assert.notNull(subscriber, "The subscriber must not be null");
        Assert.notNull(interestReason, "The interest reason must not be null");
        var reasonType = interestReason.getReasonType();
        var expression = interestReason.getExpression();
        var subject = interestReason.getSubject();

        var listOptions = new ListOptions();
        var fieldQuery = and(isNull("metadata.deletionTimestamp"),
            equal("spec.subscriber", subscriber.toString()),
            equal("spec.reason.reasonType", reasonType));

        if (subject != null) {
            fieldQuery = and(fieldQuery, reasonSubjectMatch(subject));
        }
        if (StringUtils.isNotBlank(expression)) {
            fieldQuery = and(fieldQuery, equal("spec.reason.expression", expression));
        }
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return paginatedOperator.deleteInitialBatch(Subscription.class, listOptions).then();
    }

    @Override
    public Mono<Void> remove(Subscription.Subscriber subscriber) {
        var listOptions = new ListOptions();
        var fieldQuery = and(isNull("metadata.deletionTimestamp"),
            equal("spec.subscriber", subscriber.toString()));
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return paginatedOperator.deleteInitialBatch(Subscription.class, listOptions)
            .then();
    }

    @Override
    public Mono<Subscription> remove(Subscription subscription) {
        return client.delete(subscription)
            .onErrorResume(OptimisticLockingFailureException.class,
                e -> attemptToDelete(subscription.getMetadata().getName()));
    }

    @Override
    public Flux<Subscription> removeBy(ListOptions listOptions) {
        return paginatedOperator.deleteInitialBatch(Subscription.class, listOptions);
    }

    @Override
    public Flux<Subscription> listByPerPage(String reasonType) {
        final var listOptions = new ListOptions();
        var fieldQuery = and(isNull("metadata.deletionTimestamp"),
            equal("spec.reason.reasonType", reasonType));
        listOptions.setFieldSelector(FieldSelector.of(fieldQuery));
        return paginatedOperator.list(Subscription.class, listOptions);
    }

    private Mono<Subscription> attemptToDelete(String subscriptionName) {
        return Mono.defer(() -> client.fetch(Subscription.class, subscriptionName)
                .flatMap(client::delete)
            )
            .retryWhen(Retry.backoff(8, Duration.ofMillis(100))
                .filter(OptimisticLockingFailureException.class::isInstance));
    }

    Query reasonSubjectMatch(Subscription.ReasonSubject reasonSubject) {
        Assert.notNull(reasonSubject, "The reasonSubject must not be null");
        if (StringUtils.isNotBlank(reasonSubject.getName())) {
            return equal("spec.reason.subject", reasonSubject.toString());
        }
        var matchAllSubject = new Subscription.ReasonSubject();
        matchAllSubject.setKind(reasonSubject.getKind());
        matchAllSubject.setApiVersion(reasonSubject.getApiVersion());
        return startsWith("spec.reason.subject", matchAllSubject.toString());
    }
}
