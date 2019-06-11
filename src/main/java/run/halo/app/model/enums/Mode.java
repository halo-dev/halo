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

    @JsonValue
    String getValue() {
        return this.name().toLowerCase();
    }

    /**
     * Get mode from value.
     *
     * @param value mode value
     * @return runtime mode or null if the value is mismatch
     */
    @Nullable
    @JsonCreator
    public static Mode valueFrom(@Nullable String value) {
        if (StringUtils.isBlank(value) || value.equalsIgnoreCase("prod")) {
            return Mode.PRODUCTION;
        }

        if (value.equalsIgnoreCase("dev")) {
            return Mode.DEVELOPMENT;
        }

        if (value.equalsIgnoreCase("test")) {
            return Mode.TEST;
        }

        return null;
    }
}
