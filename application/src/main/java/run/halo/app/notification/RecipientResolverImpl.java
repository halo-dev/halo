package run.halo.app.notification;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import com.google.common.base.Throwables;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.DataBindingPropertyAccessor;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.integration.json.JsonPropertyAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import run.halo.app.core.extension.notification.Reason;
import run.halo.app.core.extension.notification.Subscription;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecipientResolverImpl implements RecipientResolver {
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final EvaluationContext evaluationContext = createEvaluationContext();
    private final SubscriptionService subscriptionService;

    @Override
    public Flux<Subscriber> resolve(Reason reason) {
        var reasonType = reason.getSpec().getReasonType();
        return subscriptionService.listByPerPage(reasonType)
            .filter(this::isNotDisabled)
            .filter(subscription -> {
                var interestReason = subscription.getSpec().getReason();
                if (hasSubject(interestReason)) {
                    return subjectMatch(subscription, reason.getSpec().getSubject());
                } else if (StringUtils.isNotBlank(interestReason.getExpression())) {
                    return expressionMatch(subscription.getMetadata().getName(),
                        interestReason.getExpression(), reason);
                }
                return false;
            })
            .map(subscription -> {
                var id = UserIdentity.of(subscription.getSpec().getSubscriber().getName());
                return new Subscriber(id, subscription.getMetadata().getName());
            })
            .distinct(Subscriber::name);
    }

    boolean hasSubject(Subscription.InterestReason interestReason) {
        return !Subscription.InterestReason.isFallbackSubject(interestReason.getSubject());
    }

    boolean expressionMatch(String subscriptionName, String expressionStr, Reason reason) {
        try {
            Expression expression =
                expressionParser.parseExpression(expressionStr);
            var result = expression.getValue(evaluationContext,
                exprRootObject(reason),
                Boolean.class);
            return BooleanUtils.isTrue(result);
        } catch (ParseException | EvaluationException e) {
            log.debug("Failed to parse or evaluate expression for subscription [{}], skip it.",
                subscriptionName, Throwables.getRootCause(e));
            return false;
        }
    }

    Map<String, Object> exprRootObject(Reason reason) {
        var map = new HashMap<String, Object>(3, 1);
        map.put("props", defaultIfNull(reason.getSpec().getAttributes(), new ReasonAttributes()));
        map.put("subject", reason.getSpec().getSubject());
        map.put("author", reason.getSpec().getAuthor());
        return Collections.unmodifiableMap(map);
    }

    static boolean subjectMatch(Subscription subscription, Reason.Subject reasonSubject) {
        Assert.notNull(subscription, "The subscription must not be null");
        Assert.notNull(reasonSubject, "The reasonSubject must not be null");
        final var sourceSubject = subscription.getSpec().getReason().getSubject();

        var matchSubject = new Subscription.ReasonSubject();
        matchSubject.setKind(reasonSubject.getKind());
        matchSubject.setApiVersion(reasonSubject.getApiVersion());

        if (StringUtils.isBlank(sourceSubject.getName())) {
            return sourceSubject.equals(matchSubject);
        }
        matchSubject.setName(reasonSubject.getName());
        return sourceSubject.equals(matchSubject);
    }

    boolean isNotDisabled(Subscription subscription) {
        return !subscription.getSpec().isDisabled();
    }

    EvaluationContext createEvaluationContext() {
        return SimpleEvaluationContext.forPropertyAccessors(
                DataBindingPropertyAccessor.forReadOnlyAccess(),
                new MapAccessor(),
                new JsonPropertyAccessor()
            )
            .withConversionService(DefaultConversionService.getSharedInstance())
            .build();
    }
}
