package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.Data;
import org.springframework.util.Assert;
import run.halo.app.infra.utils.GenericClassUtils;

@Data
public class ListResult<T> implements Iterable<T>, Supplier<Stream<T>> {

    @Schema(description = "Page number, starts from 1. If not set or equal to 0, it means no "
        + "pagination.", required = true)
    private final int page;

    @Schema(description = "Size of each page. If not set or equal to 0, it means no pagination.",
        required = true)
    private final int size;

    @Schema(description = "Total elements.", required = true)
    private final long total;

    @Schema(description = "A chunk of items.", required = true)
    private final List<T> items;

    public ListResult(int page, int size, long total, List<T> items) {
        Assert.isTrue(total >= 0, "Total elements must be greater than or equal to 0");
        if (page < 0) {
            page = 0;
        }
        if (size < 0) {
            size = 0;
        }
        if (items == null) {
            items = Collections.emptyList();
        }
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }

    public ListResult(List<T> items) {
        this(0, 0, items.size(), items);
    }

    @Schema(description = "Indicates whether current page is the first page.", required = true)
    public boolean isFirst() {
        return !hasPrevious();
    }

    @Schema(description = "Indicates whether current page is the last page.", required = true)
    public boolean isLast() {
        return !hasNext();
    }

    @Schema(description = "Indicates whether current page has previous page.", required = true)
    @JsonProperty("hasNext")
    public boolean hasNext() {
        if (page <= 0) {
            return false;
        }
        return page < getTotalPages();
    }

    @Schema(description = "Indicates whether current page has previous page.", required = true)
    @JsonProperty("hasPrevious")
    public boolean hasPrevious() {
        return page > 1;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    @Schema(description = "Indicates total pages.", required = true)
    @JsonProperty("totalPages")
    public long getTotalPages() {
        return size == 0 ? 1 : (total + size - 1) / size;
    }

    /**
     * Generate generic ListResult class. Like {@code ListResult<User>}, {@code ListResult<Post>},
     * etc.
     *
     * @param scheme scheme of the generic type.
     * @return generic ListResult class.
     */
    public static Class<?> generateGenericClass(Scheme scheme) {
        return GenericClassUtils.generateConcreteClass(ListResult.class,
            scheme.type(),
            () -> {
                var pkgName = scheme.type().getPackageName();
                return pkgName + '.' + scheme.groupVersionKind().kind() + "List";
            });
    }

    /**
     * Generate generic ListResult class. Like {@code ListResult<User>}, {@code ListResult<Post>},
     * etc.
     *
     * @param type the generic type of {@link ListResult}.
     * @return generic ListResult class.
     */
    public static <T> Class<?> generateGenericClass(Class<T> type) {
        return GenericClassUtils.generateConcreteClass(ListResult.class, type,
            () -> type.getName() + "List");
    }

    public static <T> ListResult<T> emptyResult() {
        return new ListResult<>(List.of());
    }

    /**
     * Manually paginate the List collection.
     */
    public static <T> List<T> subList(List<T> list, int page, int size) {
        if (page < 1) {
            return list;
        }
        List<T> listSort = new ArrayList<>();
        int total = list.size();
        int pageStart = page == 1 ? 0 : (page - 1) * size;
        int pageEnd = Math.min(total, page * size);
        if (total > pageStart) {
            listSort = list.subList(pageStart, pageEnd);
        }
        return listSort;
    }

    @Override
    public Stream<T> get() {
        return items.stream();
    }
}
