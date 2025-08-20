package run.halo.app.extension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

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

    @Test
    void serializationTest() throws JsonProcessingException {
        var result = new ListResult<>(1, 10, 100, List.of("a", "b", "c"));
        var json = JsonMapper.builder()
            .build()
            .writeValueAsString(result);
        JSONAssert.assertEquals("""
            {
              "page": 1,
              "size": 10,
              "total": 100,
              "items": [
                "a",
                "b",
                "c"
              ],
              "first": true,
              "last": false,
              "hasNext": true,
              "hasPrevious": false,
              "totalPages": 10
            }
            """, json, true);
    }

    @Test
    void deserializationTest() throws JsonProcessingException {
        var json = """
            {
              "page": 2,
              "size": 10,
              "total": 100,
              "items": [
                "a",
                "b",
                "c"
              ],
              "first": false,
              "last": false,
              "hasNext": true,
              "hasPrevious": true,
              "totalPages": 10
            }
            """;
        var result = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build()
            .readValue(json, new TypeReference<ListResult<String>>() {
            });
        assertEquals(2, result.getPage());
        assertEquals(100, result.getTotal());
        assertEquals(10, result.getTotalPages());
        assertEquals(10, result.getSize());
        assertFalse(result.isFirst());
        assertFalse(result.isLast());
        assertTrue(result.hasNext());
        assertTrue(result.hasPrevious());
        assertEquals(List.of("a", "b", "c"), result.getItems());
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