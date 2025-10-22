package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Sort;

class PageRequestImplTest {

    @ParameterizedTest
    @ValueSource(
        ints = {0, -1, -10, -100, -1000, -10000}
    )
    void shouldBeCompatibleZeroAndNegativePageNumber(int pageNumber) {
        var page = new PageRequestImpl(pageNumber, 10, Sort.unsorted());
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getPageSize());
    }

    @ParameterizedTest
    @ValueSource(
        ints = {0, -1, -10, -100, -1000, -10000}
    )
    void shouldBeCompatibleNegativePageSize(int size) {
        var page = new PageRequestImpl(10, size, Sort.unsorted());
        assertEquals(10, page.getPageNumber());
        assertEquals(PageRequestImpl.MAX_SIZE, page.getPageSize());
    }

}