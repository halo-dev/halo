package run.halo.app.extension;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import lombok.Data;
import org.springframework.data.util.Streamable;
import org.springframework.util.Assert;

@Data
public class ListResult<T> implements Streamable<T> {

    private final int page;

    private final int size;

    private final long total;

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

    public boolean isFirst() {
        return !hasPrevious();
    }

    public boolean isLast() {
        return !hasNext();
    }

    @JsonProperty("hasNext")
    public boolean hasNext() {
        if (page <= 0) {
            return false;
        }
        var totalPages = size == 0 ? 1 : (int) Math.ceil((double) total / (double) size);
        return page < totalPages;
    }

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
}
