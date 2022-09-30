package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

@Data
public class ListResult<T> implements Streamable<T> {

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
        var totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        return page < totalPages;
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

    @Override
    @JsonIgnore
    public boolean isEmpty() {
        return Streamable.super.isEmpty();
    }

    /**
     * Generate generic ListResult class. Like {@code ListResult<User>}, {@code ListResult<Post>},
     * etc.
     *
     * @param scheme scheme of the generic type.
     * @return generic ListResult class.
     */
    public static Class<?> generateGenericClass(Scheme scheme) {
        var generic =
            TypeDescription.Generic.Builder.parameterizedType(ListResult.class, scheme.type())
                .build();
        return new ByteBuddy()
            .subclass(generic)
            .name(scheme.groupVersionKind().kind() + "List")
            .make()
            .load(ListResult.class.getClassLoader())
            .getLoaded();
    }

    /**
     * Generate generic ListResult class. Like {@code ListResult<User>}, {@code ListResult<Post>},
     * etc.
     *
     * @param type the generic type of {@link ListResult}.
     * @return generic ListResult class.
     */
    public static <T> Class<?> generateGenericClass(Class<T> type) {
        var generic =
            TypeDescription.Generic.Builder.parameterizedType(ListResult.class, type)
                .build();
        return new ByteBuddy()
            .subclass(generic)
            .name(type.getSimpleName() + "List")
            .make()
            .load(ListResult.class.getClassLoader())
            .getLoaded();
    }

    public static <T> ListResult<T> emptyResult() {
        return new ListResult<>(List.of());
    }
}
