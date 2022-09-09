package run.halo.app.theme.router;

import java.util.List;
import lombok.Getter;
import lombok.ToString;
import run.halo.app.extension.ListResult;

/**
 * Page wrapper with next and previous url.
 *
 * @param <T> the type of the list item.
 * @author guqing
 * @since 2.0.0
 */
@Getter
@ToString(callSuper = true)
public class UrlContextListResult<T> extends ListResult<T> {
    private final String nextUrl;
    private final String prevUrl;

    public UrlContextListResult(int page, int size, long total, List<T> items, String nextUrl,
        String prevUrl) {
        super(page, size, total, items);
        this.nextUrl = nextUrl;
        this.prevUrl = prevUrl;
    }

    public static class Builder<T> {
        private int page;
        private int size;
        private long total;
        private List<T> items;
        private String nextUrl;
        private String prevUrl;

        public Builder<T> page(int page) {
            this.page = page;
            return this;
        }

        public Builder<T> size(int size) {
            this.size = size;
            return this;
        }

        public Builder<T> total(long total) {
            this.total = total;
            return this;
        }

        public Builder<T> items(List<T> items) {
            this.items = items;
            return this;
        }

        public Builder<T> nextUrl(String nextUrl) {
            this.nextUrl = nextUrl;
            return this;
        }

        public Builder<T> prevUrl(String prevUrl) {
            this.prevUrl = prevUrl;
            return this;
        }

        /**
         * Assign value with list result.
         *
         * @param listResult list result
         * @return builder
         */
        public Builder<T> listResult(ListResult<T> listResult) {
            this.page = listResult.getPage();
            this.size = listResult.getSize();
            this.total = listResult.getTotal();
            this.items = listResult.getItems();
            return this;
        }

        public UrlContextListResult<T> build() {
            return new UrlContextListResult<>(page, size, total, items, nextUrl, prevUrl);
        }
    }
}
