package run.halo.app.infra;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * EqualsAndHashCode 排除了lastTransitionTime否则失败时，lastTransitionTime 会被更新
 * 导致 equals 为 false，一直被加入队列.
 *
 * @author guqing
 * @see
 * <a href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">pod-conditions</a>
 * @since 2.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "lastTransitionTime")
public class Condition {
    /**
     * type of condition in CamelCase or in foo.example.com/CamelCase.
     * example: Ready, Initialized.
     * maxLength: 316.
     */
    @Schema(required = true, maxLength = 316,
        pattern = "^([a-z0-9]([-a-z0-9]*[a-z0-9])?(\\.[a-z0-9]([-a-z0-9]*[a-z0-9])?)*/)?("
            + "([A-Za-z0-9][-A-Za-z0-9_.]*)?[A-Za-z0-9])$")
    private String type;

    /**
     * Status is the status of the condition. Can be True, False, Unknown.
     */
    @Schema(required = true)
    private ConditionStatus status;

    /**
     * Last time the condition transitioned from one status to another.
     */
    @Schema(required = true)
    private Instant lastTransitionTime;

    /**
     * Human-readable message indicating details about last transition.
     * This may be an empty string.
     */
    @Schema(required = true, maxLength = 32768)
    private String message;

    /**
     * Unique, one-word, CamelCase reason for the condition's last transition.
     */
    @Schema(required = true, maxLength = 1024,
        pattern = "^[A-Za-z]([A-Za-z0-9_,:]*[A-Za-z0-9_])?$")
    private String reason;
}
