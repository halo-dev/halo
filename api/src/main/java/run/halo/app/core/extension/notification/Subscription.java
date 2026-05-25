package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;
import static org.apache.commons.lang3.StringUtils.defaultString;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.*;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Subscription extension that records which subscriber should be notified when a matching reason is triggered.
 *
 * <p>It holds a {@link Subscriber} to the user to be notified, a {@link InterestReason} to subscribe to.
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(
        group = "notification.halo.run",
        version = "v1alpha1",
        kind = "Subscription",
        plural = "subscriptions",
        singular = "subscription")
public class Subscription extends AbstractExtension {

    /** Desired subscriber, unsubscribe token, interest expression, and disabled state. */
    @Schema
    private Spec spec;

    /** Desired notification subscription settings. */
    @Data
    @Schema(name = "SubscriptionSpec")
    public static class Spec {
        /** Subscriber that receives matching notifications. */
        @Schema(requiredMode = REQUIRED)
        private Subscriber subscriber;

        /** Token used to unsubscribe without authenticating as the subscriber. */
        @Schema(requiredMode = REQUIRED)
        private String unsubscribeToken;

        /** Reason and optional subject or expression this subscription is interested in. */
        @Schema(requiredMode = REQUIRED)
        private InterestReason reason;

        /** Whether the subscription has been disabled, usually after the subscriber unsubscribes. */
        private boolean disabled;
    }

    /** Reason selector that decides which notifications match this subscription. */
    @Data
    public static class InterestReason {
        /** ReasonType metadata.name this subscription is interested in. */
        @Schema(requiredMode = REQUIRED)
        private String reasonType;

        /** Subject this subscription is interested in. */
        @Schema(requiredMode = REQUIRED)
        private ReasonSubject subject;

        /** Optional expression used to match reasons more flexibly than subject matching. */
        @Schema(requiredMode = NOT_REQUIRED)
        private String expression;

        /**
         * Since 2.15.0, we have added a new field <code>expression</code> to the <code>InterestReason</code> object, so
         * <code>subject</code> can be null.
         *
         * <p>In this particular scenario, when the <code>subject</code> is null, we assign it a default <code>
         * ReasonSubject</code> object. The properties of this object are set to specific values that do not occur in
         * actual applications, thus we can consider this as <code>nonexistent data</code>. The purpose of this approach
         * is to maintain backward compatibility, even if the <code>subject</code> can be null in the new version of the
         * code.
         */
        public static void ensureSubjectHasValue(InterestReason interestReason) {
            if (interestReason.getSubject() == null) {
                interestReason.setSubject(createFallbackSubject());
            }
        }

        /** Check if the given reason subject is a fallback subject. */
        public static boolean isFallbackSubject(ReasonSubject reasonSubject) {
            if (reasonSubject == null) {
                return true;
            }
            var fallback = createFallbackSubject();
            return fallback.getKind().equals(reasonSubject.getKind())
                    && fallback.getApiVersion().equals(reasonSubject.getApiVersion());
        }

        static ReasonSubject createFallbackSubject() {
            return ReasonSubject.builder()
                    .apiVersion("notification.halo.run/v1alpha1")
                    .kind("NonexistentKind")
                    .build();
        }
    }

    /** Subject selector used by a subscription interest reason. */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(name = "InterestReasonSubject")
    public static class ReasonSubject {

        /** Subject metadata.name. If omitted, all subjects of the selected kind and API version are matched. */
        @Schema(requiredMode = NOT_REQUIRED)
        private String name;

        /** Subject API version. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String apiVersion;

        /** Subject kind. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String kind;

        @Override
        public String toString() {
            return kind + "#" + apiVersion + "/" + defaultString(name);
        }
    }

    /** Subscriber that receives notifications. */
    @Data
    @Schema(name = "SubscriptionSubscriber")
    public static class Subscriber {
        /** User metadata.name of the subscriber. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String name;

        @Override
        public String toString() {
            return name;
        }
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
