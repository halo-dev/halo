package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ListResultTest {

    @Test
    void generateGenericClass() {
        var fakeListClass =
            ListResult.generateGenericClass(Scheme.buildFromType(FakeExtension.class));
        assertTrue(ListResult.class.isAssignableFrom(fakeListClass));
        assertSame(FakeExtension.class, ((ParameterizedType) fakeListClass.getGenericSuperclass())
            .getActualTypeArguments()[0]);
        assertEquals("FakeList", fakeListClass.getSimpleName());
    }

    @Test
    void generateGenericClassForClassParam() {
        var fakeListClass = ListResult.generateGenericClass(FakeExtension.class);
        assertTrue(ListResult.class.isAssignableFrom(fakeListClass));
        assertSame(FakeExtension.class, ((ParameterizedType) fakeListClass.getGenericSuperclass())
            .getActualTypeArguments()[0]);
        assertEquals("FakeExtensionList", fakeListClass.getSimpleName());
    }

    @Test
    void totalPages() {
        var listResult = new ListResult<>(1, 10, 100, List.of());
        assertEquals(10, listResult.getTotalPages());

        listResult = new ListResult<>(1, 10, 1, List.of());
        assertEquals(1, listResult.getTotalPages());

        listResult = new ListResult<>(1, 10, 9, List.of());
        assertEquals(1, listResult.getTotalPages());

        listResult = new ListResult<>(1, 0, 100, List.of());
        assertEquals(1, listResult.getTotalPages());
    }

    @Test
    void subListWhenSizeIsZero() {
        var list = List.of(1, 2, 3, 4, 5);
        assertSubList(list);

        list = List.of(1);
        assertSubList(list);
    }

    @Test
    void firstTest() {
        var listResult = new ListResult<>(List.of());
        assertEquals(Optional.empty(), ListResult.first(listResult));

        listResult = new ListResult<>(1, 10, 1, List.of("A"));
        assertEquals(Optional.of("A"), ListResult.first(listResult));
    }

    private void assertSubList(List<Integer> list) {
        var result = ListResult.subList(list, 0, 0);
        assertEquals(list, result);

        result = ListResult.subList(list, 0, 1);
        assertEquals(list.subList(0, 1), result);

        result = ListResult.subList(list, 1, 0);
        assertEquals(list, result);

        assertEquals(list.subList(0, 1), ListResult.subList(list, -1, 1));
    }
}