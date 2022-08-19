package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.ParameterizedType;
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
}