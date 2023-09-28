package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.StringUtils.defaultString;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * <p>{@link Subscription} is a custom extension that defines a subscriber to be notified when a
 * certain {@link Reason} is triggered.</p>
 * <p>It holds a {@link Subscriber} to the user to be notified, a {@link InterestReason} to
 * subscribe to.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "Subscription",
    plural = "subscriptions", singular = "subscription")
public class Subscription extends AbstractExtension {

    @Schema
    private Spec spec;

    @Data
    @Schema(name = "SubscriptionSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED, description = "The subscriber to be notified")
        private Subscriber subscriber;

        @Schema(requiredMode = REQUIRED, description = "The token to unsubscribe")
        private String unsubscribeToken;

        @Schema(requiredMode = REQUIRED, description = "The reason to be interested in")
        private InterestReason reason;

        @Schema(description = "Perhaps users need to unsubscribe and "
            + "interact without receiving notifications again")
        private boolean disabled;
    }

    @Data
    public static class InterestReason {
        @Schema(requiredMode = REQUIRED, description = "The name of the reason definition to be "
            + "interested in")
        private String reasonType;

        @Schema(requiredMode = REQUIRED, description = "The subject name of reason type to be"
            + " interested in")
        private ReasonSubject subject;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "InterestReasonSubject")
    public static class ReasonSubject {

        @Schema(requiredMode = NOT_REQUIRED, description = "if name is not specified, it presents "
            + "all subjects of the specified reason type and custom resources")
        private String name;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String apiVersion;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String kind;

        @Override
        public String toString() {
            return kind + "#" + apiVersion + "/" + defaultString(name);
        }
    }

    @Data
    @ToString
    @Schema(name = "SubscriptionSubscriber")
    public static class Subscriber {
        private String name;
    }

    /**
     * Generate unsubscribe token for unsubscribe.
     *
     * @return unsubscribe token
     */
    public static String generateUnsubscribeToken() {
        return UUID.randomUUID().toString();
    }
}
