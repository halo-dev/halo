package run.halo.app.core.extension.notification;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;
import run.halo.app.notification.ReasonAttributes;

/**
 * <p>{@link Reason} is a custom extension that defines a reason for a notification, It represents
 * an instance of a {@link ReasonType}.</p>
 * <p>It can be understood as an event that triggers a notification.</p>
 *
 * @author guqing
 * @since 2.10.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = "notification.halo.run", version = "v1alpha1", kind = "Reason", plural =
    "reasons", singular = "reason")
public class Reason extends AbstractExtension {

    @Schema
    private Spec spec;

    @Data
    @Accessors(chain = true)
    @Schema(name = "ReasonSpec")
    public static class Spec {
        @Schema(requiredMode = REQUIRED)
        private String reasonType;

        @Schema(requiredMode = REQUIRED)
        private Subject subject;

        @Schema(requiredMode = REQUIRED)
        private String author;

        @Schema(implementation = ReasonAttributes.class, requiredMode = NOT_REQUIRED,
            description = "Attributes used to transfer data")
        private ReasonAttributes attributes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "ReasonSubject")
    public static class Subject {
        @Schema(requiredMode = REQUIRED)
        private String apiVersion;

        @Schema(requiredMode = REQUIRED)
        private String kind;

        @Schema(requiredMode = REQUIRED)
        private String name;

        @Schema(requiredMode = REQUIRED)
        private String title;

        private String url;
    }
}
