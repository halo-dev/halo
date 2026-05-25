package run.halo.app.core.extension;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jspecify.annotations.Nullable;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/** Device extension that records an authenticated browser session for account security visibility. */
@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = Device.GROUP, version = Device.VERSION, kind = Device.KIND, plural = "devices", singular = "device")
public class Device extends AbstractExtension {
    public static final String GROUP = "security.halo.run";
    public static final String VERSION = "v1alpha1";
    public static final String KIND = "Device";

    /** Desired device/session identity and access timestamps. */
    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    /** Resolved client environment details for display. */
    @Schema(requiredMode = REQUIRED)
    private Status status = new Status();

    public void setStatus(@Nullable Status status) {
        this.status = (status == null ? new Status() : status);
    }

    /** Session identity and request metadata captured for a device. */
    @Data
    @Accessors(chain = true)
    @Schema(name = "DeviceSpec")
    public static class Spec {

        /** Web session identifier associated with the device. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String sessionId;

        /** Principal name of the authenticated user. */
        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String principalName;

        /** Client IP address observed for the session. */
        @Schema(requiredMode = REQUIRED, maxLength = 129)
        private String ipAddress;

        /** Client user-agent header captured for the session. */
        @Schema(maxLength = 500)
        private String userAgent;

        /** Remember-me series id associated with the session, if any. */
        @Nullable
        private String rememberMeSeriesId;

        /** Last time this session accessed Halo. */
        private Instant lastAccessedTime;

        /** Last time the user authenticated for this session. */
        private Instant lastAuthenticatedTime;
    }

    /** Parsed browser and operating system details for display. */
    @Data
    @Accessors(chain = true)
    @Schema(name = "DeviceStatus")
    public static class Status {
        /** Browser name parsed from the user agent. */
        private String browser;

        /** Operating system name parsed from the user agent. */
        private String os;
    }
}
