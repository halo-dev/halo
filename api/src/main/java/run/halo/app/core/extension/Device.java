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

@Data
@EqualsAndHashCode(callSuper = true)
@GVK(group = Device.GROUP, version = Device.VERSION, kind = Device.KIND, plural = "devices",
    singular = "device")
public class Device extends AbstractExtension {
    public static final String GROUP = "security.halo.run";
    public static final String VERSION = "v1alpha1";
    public static final String KIND = "Device";

    @Schema(requiredMode = REQUIRED)
    private Spec spec;

    @Schema(requiredMode = REQUIRED)
    private Status status = new Status();

    public void setStatus(@Nullable Status status) {
        this.status = (status == null ? new Status() : status);
    }

    @Data
    @Accessors(chain = true)
    @Schema(name = "DeviceSpec")
    public static class Spec {

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String sessionId;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String principalName;

        @Schema(requiredMode = REQUIRED, maxLength = 129)
        private String ipAddress;

        @Schema(maxLength = 500)
        private String userAgent;

        private String rememberMeSeriesId;

        private Instant lastAccessedTime;

        private Instant lastAuthenticatedTime;
    }

    @Data
    @Accessors(chain = true)
    @Schema(name = "DeviceStatus")
    public static class Status {
        private String browser;
        private String os;
    }
}
