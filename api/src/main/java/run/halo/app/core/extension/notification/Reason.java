package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.notification.ReasonAttributes;

/**
 * Reason extension that represents one notification event instance of a {@link ReasonType}.
 *
 * <p>It can be understood as an event that triggers a notification.
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "Reason", plural = "reasons", singular = "reason")
public class Reason extends AbstractExtension {

    /** Desired notification reason data, including type, subject, author, and template attributes. */
    @Schema
    private Spec spec;

    /** Notification reason data used to render and dispatch notifications. */
    @Data
    @Accessors(chain = true)
    @Schema(name = "ReasonSpec")
    public static class Spec {
        /** ReasonType metadata.name that defines this reason. */
        @Schema(requiredMode = REQUIRED)
        private String reasonType;

        /** Subject resource this reason is about. */
        @Schema(requiredMode = REQUIRED)
        private Subject subject;

        /** User metadata.name or system actor that created the reason. */
        @Schema(requiredMode = REQUIRED)
        private String author;

        /** Additional key-value attributes passed to notification templates. */
        @Schema(implementation = ReasonAttributes.class, requiredMode = NOT_REQUIRED)
        private ReasonAttributes attributes;
    }

    /** Resource subject associated with a notification reason. */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReasonSubject")
    public static class Subject {
        /** Subject API version. */
        @Schema(requiredMode = REQUIRED)
        private String apiVersion;

        /** Subject kind. */
        @Schema(requiredMode = REQUIRED)
        private String kind;

        /** Subject metadata.name. */
        @Schema(requiredMode = REQUIRED)
        private String name;

        /** Human-readable subject title shown in notifications. */
        @Schema(requiredMode = REQUIRED)
        private String title;

        /** URL that opens the subject. */
        private String url;
    }
}
