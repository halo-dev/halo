package run.halo.app.theme.endpoint;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import run.halo.app.extension.Extension;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.ListResult;

/**
 * Utility class for public api.
 *
 * @author guqing
 * @since 2.5.0
 */
@UtilityClass
public class PublicApiUtils {

    /**
     * Get group version from extension for public api.
     *
     * @param extension extension
     * @return <code>api.{group}/{version}</code> if group is not empty,
     * otherwise <code>api.halo.run/{version}</code>.
     */
    public static GroupVersion groupVersion(Extension extension) {
        GroupVersionKind groupVersionKind = extension.groupVersionKind();
        String group = StringUtils.defaultIfBlank(groupVersionKind.group(), "halo.run");
        return new GroupVersion("api." + group, groupVersionKind.version());
    }

    /**
     * Converts list result to another list result.
     *
     * @param listResult list result to be converted
     * @param mapper mapper function to convert item
     * @param <T> item type
     * @param <R> converted item type
     * @return converted list result
     */
    public static <T, R> ListResult<R> toAnotherListResult(ListResult<T> listResult,
        Function<T, R> mapper) {
        Assert.notNull(listResult, "List result must not be null");
        Assert.notNull(mapper, "The mapper must not be null");
        List<R> mappedItems = listResult.get()
            .map(mapper)
            .toList();
        return new ListResult<>(listResult.getPage(), listResult.getSize(), listResult.getTotal(),
            mappedItems);
    }

    /**
     * Checks whether collection contains element.
     *
     * @param <T> element type
     * @return true if collection contains element, otherwise false.
     */
    public static <T> boolean containsElement(@Nullable Collection<T> collection,
        @Nullable T element) {
        if (collection != null && element != null) {
            return collection.contains(element);
        }
        return false;
    }
}
