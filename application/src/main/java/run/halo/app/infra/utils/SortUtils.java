package run.halo.app.infra.utils;

import java.util.List;
import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

@UtilityClass
public class SortUtils {
    static final String delimiter = ",";

    /**
     * <p>Resolve from direction params, e.g. "name,asc" or "name"</p>
     *
     * @param directionParams direction params
     * @return sort object
     */
    public static Sort resolve(List<String> directionParams) {
        if (CollectionUtils.isEmpty(directionParams)) {
            return Sort.unsorted();
        }
        Sort.Order[] orders = new Sort.Order[directionParams.size()];
        for (int i = 0; i < directionParams.size(); i++) {
            String[] parts = directionParams.get(i).split(delimiter);
            if (parts.length == 1) {
                orders[i] = new Sort.Order(Sort.Direction.ASC, parts[0]);
            } else {
                orders[i] = new Sort.Order(toDirection(parts[1]), parts[0]);
            }
        }
        return Sort.by(orders);
    }

    private static Sort.Direction toDirection(@NonNull String direction) {
        Assert.notNull(direction, "Direction must not be null");
        if (direction.contains(" ")) {
            throw new IllegalArgumentException("Direction must not contain whitespace");
        }
        return Sort.Direction.fromString(direction);
    }
}
