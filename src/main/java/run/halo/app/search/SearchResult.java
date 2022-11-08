package run.halo.app.search;

import java.util.List;
import lombok.Data;

@Data
public class SearchResult<T> {
    private List<T> hits;
    private String keyword;
    private Long total;
    private int limit;
    private long processingTimeMillis;
}
