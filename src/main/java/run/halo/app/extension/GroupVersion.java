package run.halo.app.extension;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * GroupVersion contains group and version name of an Extension only.
 *
 * @param group is group name of Extension.
 * @param version is version name of Extension.
 * @author johnniang
 */
public record GroupVersion(String group, String version) {

    @Override
    public String toString() {
        return StringUtils.hasText(group) ? group + "/" + version : version;
    }

    /**
     * Parses APIVersion into GroupVersion record.
     *
     * @param apiVersion must not be blank.
     * 1. If the given apiVersion does not contain any "/", we treat the group is empty.
     * 2. If the given apiVersion contains more than 1 "/", we will throw an
     * IllegalArgumentException.
     * @return record contains group and version.
     */
    public static GroupVersion parseAPIVersion(String apiVersion) {
        Assert.hasText(apiVersion, "API version must not be blank");

        var groupVersion = apiVersion.split("/");
        return switch (groupVersion.length) {
            case 1 -> new GroupVersion("", apiVersion);
            case 2 -> new GroupVersion(groupVersion[0], groupVersion[1]);
            default ->
                throw new IllegalArgumentException("Unexpected APIVersion string: " + apiVersion);
        };
    }
}
