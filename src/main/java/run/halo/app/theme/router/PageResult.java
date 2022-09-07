package run.halo.app.theme.router;

import java.util.List;
import run.halo.app.extension.ListResult;

public class PageResult<T> extends ListResult<T> {
    private final String nextUrl;
    private final String prevUrl;

    public PageResult(int page, int size, long total, List<T> items, String nextUrl,
        String prevUrl) {
        super(page, size, total, items);
        this.nextUrl = nextUrl;
        this.prevUrl = prevUrl;
    }

    public PageResult(List<T> items, String nextUrl, String prevUrl) {
        super(items);
        this.nextUrl = nextUrl;
        this.prevUrl = prevUrl;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public String getPrevUrl() {
        return prevUrl;
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

        public PageResult<T> build() {
            return new PageResult<>(page, size, total, items, nextUrl, prevUrl);
        }
    }
}
