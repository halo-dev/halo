package run.halo.app.extension.index.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.index.Index;
import run.halo.app.extension.index.Indices;
import run.halo.app.extension.index.LabelIndexQuery;
import run.halo.app.extension.index.ValueIndexQuery;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class QueryVisitorTest {

    @Mock
    Indices<FakeExtension> indices;

    @Spy
    ConversionService conversionService = ApplicationConversionService.getSharedInstance();

    @InjectMocks
    QueryVisitor<FakeExtension> visitor;

    @Test
    void shouldVisitAllCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all", "data"));
        var condition = Queries.all("metadata.name");
        condition.visit(visitor);
        assertEquals(Set.of("all", "data"), visitor.getResult());
    }

    @Test
    void shouldVisitNoneCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        when(indices.getIndex("metadata.name")).thenReturn(index);
        var condition = Queries.all("metadata.name").not();
        condition.visit(visitor);
        assertEquals(Set.of(), visitor.getResult());
    }

    @Test
    void shouldVisitEmptyCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all", "data"));
        var condition = Condition.empty();
        condition.visit(visitor);
        assertEquals(Set.of("all", "data"), visitor.getResult());
    }

    @Test
    void shouldVisitEqualsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.equal("test-name")).thenReturn(Set.of("equal", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.equal("metadata.name", "test-name");
        condition.visit(visitor);
        assertEquals(Set.of("equal", "data"), visitor.getResult());

        verify(conversionService).canConvert(String.class, String.class);
        verify(conversionService).convert("test-name", String.class);
    }

    @Test
    void shouldVisitNotEqualsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.notEqual("test-name")).thenReturn(Set.of("not-equal", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.notEqual("metadata.name", "test-name");
        condition.visit(visitor);
        assertEquals(Set.of("not-equal", "data"), visitor.getResult());

        verify(conversionService).canConvert(String.class, String.class);
        verify(conversionService).convert("test-name", String.class);
    }

    @Test
    void shouldVisitInCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.in(argThat(c -> c.containsAll(List.of("name1", "name1")))))
            .thenReturn(Set.of("in", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.in("metadata.name", Set.<String>of("name1", "name2"));
        condition.visit(visitor);
        assertEquals(Set.of("in", "data"), visitor.getResult());
        verify(conversionService, times(2)).canConvert(String.class, String.class);
        verify(conversionService).convert("name1", String.class);
        verify(conversionService).convert("name2", String.class);
    }

    @Test
    void shouldVisitNotInCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.notIn(argThat(c -> c.containsAll(List.of("name1", "name2")))))
            .thenReturn(Set.of("not-in", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.in("metadata.name", Set.<String>of("name1", "name2")).not();
        condition.visit(visitor);
        assertEquals(Set.of("not-in", "data"), visitor.getResult());
        verify(conversionService, times(2)).canConvert(String.class, String.class);
        verify(conversionService).convert("name1", String.class);
        verify(conversionService).convert("name2", String.class);
    }

    @Test
    void shouldVisitGreaterThanCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.age")).thenReturn(index);
        when(query.greaterThan(18, false)).thenReturn(Set.of("gt", "data"));
        when(index.getKeyType()).thenReturn(Integer.class);
        var condition = Queries.greaterThan("metadata.age", 18);
        condition.visit(visitor);
        assertEquals(Set.of("gt", "data"), visitor.getResult());

        verify(conversionService).canConvert(Integer.class, Integer.class);
        verify(conversionService).convert(18, Integer.class);
    }

    @Test
    void shouldVisitLessThanCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.age")).thenReturn(index);
        when(query.lessThan(65, false)).thenReturn(Set.of("lt", "data"));
        when(index.getKeyType()).thenReturn(Integer.class);
        var condition = Queries.lessThan("metadata.age", 65);
        condition.visit(visitor);
        assertEquals(Set.of("lt", "data"), visitor.getResult());

        verify(conversionService).canConvert(Integer.class, Integer.class);
        verify(conversionService).convert(65, Integer.class);
    }

    @Test
    void shouldVisitBetweenCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.age")).thenReturn(index);
        when(query.between(18, true, 65, false)).thenReturn(Set.of("between", "data"));
        when(index.getKeyType()).thenReturn(Integer.class);
        var condition = Queries.between("metadata.age", 18, true, 65, false);
        condition.visit(visitor);
        assertEquals(Set.of("between", "data"), visitor.getResult());

        verify(conversionService, times(2)).canConvert(Integer.class, Integer.class);
        verify(conversionService).convert(18, Integer.class);
        verify(conversionService).convert(65, Integer.class);
    }

    @Test
    void shouldVisitNotBetweenCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.age")).thenReturn(index);
        when(query.notBetween(18, false, 65, true)).thenReturn(Set.of("not-between", "data"));
        when(index.getKeyType()).thenReturn(Integer.class);
        var condition = Queries.between("metadata.age", 18, true, 65, false).not();
        condition.visit(visitor);
        assertEquals(Set.of("not-between", "data"), visitor.getResult());

        verify(conversionService, times(2)).canConvert(Integer.class, Integer.class);
        verify(conversionService).convert(18, Integer.class);
        verify(conversionService).convert(65, Integer.class);
    }

    @Test
    void shouldVisitStringContainsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.description")).thenReturn(index);
        when(query.stringContains("keyword")).thenReturn(Set.of("contains", "data"));
        var condition = Queries.contains("metadata.description", "keyword");
        condition.visit(visitor);
        assertEquals(Set.of("contains", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("keyword", String.class);
    }

    @Test
    void shouldVisitStringNotContainsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.description")).thenReturn(index);
        when(query.stringNotContains("keyword")).thenReturn(Set.of("not-contains", "data"));
        var condition = Queries.contains("metadata.description", "keyword").not();
        condition.visit(visitor);
        assertEquals(Set.of("not-contains", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("keyword", String.class);
    }

    @Test
    void shouldVisitStringStartsWithCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.title")).thenReturn(index);
        when(query.stringStartsWith("prefix")).thenReturn(Set.of("starts-with", "data"));
        var condition = Queries.startsWith("metadata.title", "prefix");
        condition.visit(visitor);
        assertEquals(Set.of("starts-with", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("prefix", String.class);
    }

    @Test
    void shouldVisitStringNotStartsWithCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.title")).thenReturn(index);
        when(query.stringNotStartsWith("prefix")).thenReturn(Set.of("not-starts-with", "data"));
        var condition = Queries.startsWith("metadata.title", "prefix").not();
        condition.visit(visitor);
        assertEquals(Set.of("not-starts-with", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("prefix", String.class);
    }

    @Test
    void shouldVisitStringEndsWithCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.title")).thenReturn(index);
        when(query.stringEndsWith("suffix")).thenReturn(Set.of("ends-with", "data"));
        var condition = Queries.endsWith("metadata.title", "suffix");
        condition.visit(visitor);
        assertEquals(Set.of("ends-with", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("suffix", String.class);
    }

    @Test
    void shouldVisitStringNotEndsWithCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.title")).thenReturn(index);
        when(query.stringNotEndsWith("suffix")).thenReturn(Set.of("not-ends-with", "data"));
        var condition = Queries.endsWith("metadata.title", "suffix").not();
        condition.visit(visitor);
        assertEquals(Set.of("not-ends-with", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("suffix", String.class);
    }

    @Test
    void shouldVisitIsNullCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.optionalField")).thenReturn(index);
        when(query.isNull()).thenReturn(Set.of("is-null", "data"));
        var condition = Queries.isNull("metadata.optionalField");
        condition.visit(visitor);
        assertEquals(Set.of("is-null", "data"), visitor.getResult());
    }

    @Test
    void shouldVisitIsNotNullCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.optionalField")).thenReturn(index);
        when(query.isNotNull()).thenReturn(Set.of("is-not-null", "data"));
        var condition = Queries.isNull("metadata.optionalField").not();
        condition.visit(visitor);
        assertEquals(Set.of("is-not-null", "data"), visitor.getResult());
    }

    @Test
    void shouldVisitLabelEqualsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var query = (LabelIndexQuery) index;
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(query.equal("env", "production")).thenReturn(Set.of("label-equal", "data"));
        var condition = Queries.labelEqual("env", "production");
        condition.visit(visitor);
        assertEquals(Set.of("label-equal", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("production", String.class);
    }

    @Test
    void shouldVisitLabelNotEqualsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var primaryIndex = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (LabelIndexQuery) index;
        var primaryQuery = (ValueIndexQuery) primaryIndex;
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(indices.getIndex("metadata.name")).thenReturn(primaryIndex);
        when(query.equal("env", "production")).thenReturn(Set.of("equal", "data"));
        when(primaryQuery.all()).thenReturn(Set.of("all", "data"));
        var condition = Queries.labelEqual("env", "production").not();
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());
    }

    @Test
    void shouldVisitLabelInCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var query = (LabelIndexQuery) index;
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(query.in(eq("env"), eq(Set.of("production", "staging"))))
            .thenReturn(Set.of("label-in", "data"));
        var condition = Queries.labelIn("env", Set.of("production", "staging"));
        condition.visit(visitor);
        assertEquals(Set.of("label-in", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("production", String.class);
        verify(conversionService, never()).convert("staging", String.class);
    }

    @Test
    void shouldVisitLabelNotInCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var query = (LabelIndexQuery) index;
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(query.notIn(eq("env"), eq(Set.of("production", "staging"))))
            .thenReturn(Set.of("label-not-in", "data"));
        var condition = Queries.labelIn("env", Set.of("production", "staging")).not();
        condition.visit(visitor);
        assertEquals(Set.of("label-not-in", "data"), visitor.getResult());

        verify(conversionService, never()).canConvert(String.class, String.class);
        verify(conversionService, never()).convert("production", String.class);
        verify(conversionService, never()).convert("staging", String.class);
    }

    @Test
    void shouldVisitLabelExistsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var query = (LabelIndexQuery) index;
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(query.exists("env")).thenReturn(Set.of("label-exists", "data"));
        var condition = Queries.labelExists("env");
        condition.visit(visitor);
        assertEquals(Set.of("label-exists", "data"), visitor.getResult());
    }

    @Test
    void shouldVisitLabelNotExistsCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        var primaryIndex = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (LabelIndexQuery) index;
        var primaryQuery = (ValueIndexQuery) primaryIndex;
        when(indices.getIndex("metadata.name")).thenReturn(primaryIndex);
        when(indices.getIndex("metadata.labels")).thenReturn(index);
        when(query.exists("env")).thenReturn(Set.of("label-exists", "data"));
        when(primaryQuery.all()).thenReturn(Set.of("all", "data"));
        var condition = Queries.labelExists("env").not();
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());
    }

    @Test
    void shouldVisiteAndCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.equal("name1")).thenReturn(Set.of("name1", "data"));
        when(query.equal("name2")).thenReturn(Set.of("name2", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.equal("metadata.name", "name1")
            .and(Queries.equal("metadata.name", "name2"));
        condition.visit(visitor);
        assertEquals(Set.of("data"), visitor.getResult());
    }

    @Test
    void shouldVisiteOrCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.equal("name1")).thenReturn(Set.of("name1", "data"));
        when(query.equal("name2")).thenReturn(Set.of("name2", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.equal("metadata.name", "name1")
            .or(Queries.equal("metadata.name", "name2"));
        condition.visit(visitor);
        assertEquals(Set.of("name1", "name2", "data"), visitor.getResult());
    }

    @Test
    void shouldVisiteNotCondition() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.notEqual("name1")).thenReturn(Set.of("not-equal", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = new NotCondition(Queries.equal("metadata.name", "name1"));
        condition.visit(visitor);
        assertEquals(Set.of("not-equal", "data"), visitor.getResult());
    }

    @Test
    void shouldRefineAndConditionWithLeftEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.equal("name1")).thenReturn(Set.of("name1", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Condition.empty()
            .and(Queries.equal("metadata.name", "name1"));
        condition.visit(visitor);
        assertEquals(Set.of("name1", "data"), visitor.getResult());
    }

    @Test
    void shouldRefineAndConditionWithRightEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.equal("name1")).thenReturn(Set.of("name1", "data"));
        when(index.getKeyType()).thenReturn(String.class);
        var condition = Queries.equal("metadata.name", "name1")
            .and(Condition.empty());
        condition.visit(visitor);
        assertEquals(Set.of("name1", "data"), visitor.getResult());
    }

    @Test
    void shouldRefineOrConditionWithLeftEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all"));
        var condition = Condition.empty()
            .or(Queries.equal("metadata.name", "name1"));
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());

        verify(query, never()).equal("name1");
    }

    @Test
    void shouldRefineOrConditionWithRightEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all"));
        var condition = Queries.equal("metadata.name", "name1")
            .or(Condition.empty());
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());

        verify(query, never()).equal("name1");
    }

    @Test
    void shouldRefineAndConditionWithBothEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all"));
        var condition = Condition.empty().and(Condition.empty());
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());

        verify(query).all();
    }

    @Test
    void shouldRefineOrConditionWithBothEmpty() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        var query = (ValueIndexQuery) index;
        when(indices.getIndex("metadata.name")).thenReturn(index);
        when(query.all()).thenReturn(Set.of("all"));
        var condition = Condition.empty().or(Condition.empty());
        condition.visit(visitor);
        assertEquals(Set.of("all"), visitor.getResult());

        verify(query).all();
    }

    @Test
    void throwErrorIfIndexNotFound() {
        var condition = Queries.equal("metadata.unknownField", "value");
        when(indices.getIndex("metadata.unknownField")).thenThrow(IllegalArgumentException.class);
        assertThrows(IllegalArgumentException.class, (() -> {
            condition.visit(visitor);
        }));
    }

    @Test
    void throwErrorIfIndexTypeMismatchForValueIndexQuery() {
        var labelIndex = mock(Index.class, withSettings().extraInterfaces(LabelIndexQuery.class));
        when(indices.getIndex("metadata.name")).thenReturn(labelIndex);
        var condition = Queries.equal("metadata.name", "name1");
        assertThrows(IllegalArgumentException.class, (() -> {
            condition.visit(visitor);
        }));
    }

    @Test
    void throwErrorIfIndexTypeMismatchForLabelIndexQuery() {
        var valueIndex = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        when(indices.getIndex("metadata.labels")).thenReturn(valueIndex);
        var condition = Queries.labelEqual("env", "production");
        assertThrows(IllegalArgumentException.class, (() -> {
            condition.visit(visitor);
        }));
    }

    @Test
    void throwErrorIfValueConversionNotSupported() {
        var index = mock(Index.class, withSettings().extraInterfaces(ValueIndexQuery.class));
        when(indices.getIndex("metadata.age")).thenReturn(index);
        when(index.getKeyType()).thenReturn(Integer.class);
        var condition = Queries.equal("metadata.age", "not-an-integer");
        assertThrows(ConversionFailedException.class, (() -> {
            condition.visit(visitor);
        }));
    }
}