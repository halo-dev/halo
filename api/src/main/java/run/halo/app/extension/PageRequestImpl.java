package run.halo.app.extension;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

@Slf4j
public class PageRequestImpl implements PageRequest {

    public static final int MAX_SIZE = 1_000;

    private final int pageNumber;
    private final int pageSize;
    private final Sort sort;

    public PageRequestImpl(int pageNumber, int pageSize, Sort sort) {
        Assert.notNull(sort, "Sort must not be null");
        if (pageNumber < 1) {
            pageNumber = 1;
        }
        if (pageSize <= 0) {
            log.warn("Page size must be greater than 0, reset to default {}", MAX_SIZE);
            pageSize = MAX_SIZE;
        }
        if (pageSize > 1000) {
            log.warn("Page size must not be greater than {}, reset to {}", MAX_SIZE, MAX_SIZE);
            pageSize = MAX_SIZE;
        }
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.sort = sort;
    }

    public static PageRequestImpl of(int pageNumber, int pageSize) {
        return of(pageNumber, pageSize, Sort.unsorted());
    }

    public static PageRequestImpl of(int pageNumber, int pageSize, Sort sort) {
        return new PageRequestImpl(pageNumber, pageSize, sort);
    }

    public static PageRequestImpl ofSize(int pageSize) {
        return PageRequestImpl.of(1, pageSize);
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public PageRequest previous() {
        return getPageNumber() == 0 ? this
            : new PageRequestImpl(getPageNumber() - 1, getPageSize(), getSort());
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public PageRequest next() {
        return new PageRequestImpl(getPageNumber() + 1, getPageSize(), getSort());
    }

    @Override
    public PageRequest previousOrFirst() {
        return hasPrevious() ? previous() : first();
    }

    @Override
    public PageRequest first() {
        return new PageRequestImpl(1, getPageSize(), getSort());
    }

    @Override
    public PageRequest withPage(int pageNumber) {
        return new PageRequestImpl(pageNumber, getPageSize(), getSort());
    }

    @Override
    public PageRequestImpl withSort(Sort sort) {
        return new PageRequestImpl(getPageNumber(), getPageSize(),
            defaultIfNull(sort, Sort.unsorted()));
    }

    @Override
    public boolean hasPrevious() {
        return pageNumber > 1;
    }
}
