package run.halo.app.search;

import java.util.List;
import lombok.Data;

@Data
public class SearchResult {
    private List<HaloDocument> hits;
    private String keyword;
    private Long total;
    private int limit;
    private long processingTimeMillis;
}
