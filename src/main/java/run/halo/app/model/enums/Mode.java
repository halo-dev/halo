package run.halo.app.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/**
 * Halo runtime mode.
 *
 * @author johnniang
 * @date 19-6-10
 */
public enum Mode {
    PRODUCTION,
    DEVELOPMENT,
    TEST;

    /**
     * Get mode from value.
     *
     * @param value mode value
     * @return runtime mode or null if the value is mismatch
     */
    @Nullable
    @JsonCreator
    public static Mode valueFrom(@Nullable String value) {
        if (StringUtils.isBlank(value) || "prod".equalsIgnoreCase(value)) {
            return Mode.PRODUCTION;
        }

        if ("dev".equalsIgnoreCase(value)) {
            return Mode.DEVELOPMENT;
        }

        if ("test".equalsIgnoreCase(value)) {
            return Mode.TEST;
        }

        return null;
    }

    @JsonValue
    String getValue() {
        return this.name().toLowerCase();
    }
}
