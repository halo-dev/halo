package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
import java.util.List;
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
}