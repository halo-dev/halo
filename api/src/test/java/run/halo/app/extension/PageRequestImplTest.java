package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.data.domain.Sort;

class PageRequestImplTest {

    @RepeatedTest(10)
    void shouldBeCompatibleZeroAndNegativePageNumber() {
        var randomPageNumber = -(new Random().nextInt(0, Integer.MAX_VALUE));
        var page = new PageRequestImpl(randomPageNumber, 10, Sort.unsorted());
        assertEquals(1, page.getPageNumber());
        assertEquals(10, page.getPageSize());
    }

    @RepeatedTest(10)
    void shouldBeCompatibleNegativePageSize() {
        var randomPageSize = -(new Random().nextInt(1, Integer.MAX_VALUE));
        var page = new PageRequestImpl(10, randomPageSize, Sort.unsorted());
        assertEquals(10, page.getPageNumber());
        assertEquals(0, page.getPageSize());
    }

}