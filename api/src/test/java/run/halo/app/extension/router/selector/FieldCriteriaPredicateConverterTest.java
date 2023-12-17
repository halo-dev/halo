package run.halo.app.extension.router.selector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;

class FieldCriteriaPredicateConverterTest {

    FieldCriteriaPredicateConverter<Extension> converter;

    @BeforeEach
    void setUp() {
        converter = new FieldCriteriaPredicateConverter<>();
    }

    @Test
    void shouldConvertNameEqualsCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.Equals, Set.of("value1", "value2"));
        var predicate = converter.convert(criteria);
        assertNotNull(predicate);

        var fakeExt = new FakeExtension();
        var metadata = new Metadata();
        fakeExt.setMetadata(metadata);
        assertFalse(predicate.test(fakeExt));

        metadata.setName("value1");
        assertTrue(predicate.test(fakeExt));

        metadata.setName("value2");
        assertTrue(predicate.test(fakeExt));

        metadata.setName("invalid-value");
        assertFalse(predicate.test(fakeExt));
    }

    @Test
    void shouldConvertNameNotEqualsCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.NotEquals, Set.of("value1", "value2"));
        var predicate = converter.convert(criteria);
        assertNotNull(predicate);

        var fake = new FakeExtension();
        var metadata = new Metadata();
        fake.setMetadata(metadata);
        assertFalse(predicate.test(fake));

        metadata.setName("not-contain-value");
        assertTrue(predicate.test(fake));

        metadata.setName("value1");
        assertFalse(predicate.test(fake));

        metadata.setName("value2");
        assertFalse(predicate.test(fake));
    }

    @Test
    void shouldConvertNameInCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.IN, Set.of("value1", "value2"));
        var predicate = converter.convert(criteria);
        assertNotNull(predicate);

        var fake = new FakeExtension();
        var metadata = new Metadata();
        fake.setMetadata(metadata);
        assertFalse(predicate.test(fake));

        metadata.setName("not-contain-value");
        assertFalse(predicate.test(fake));

        metadata.setName("value1");
        assertTrue(predicate.test(fake));

        metadata.setName("value2");
        assertTrue(predicate.test(fake));
    }

    @Test
    void shouldReturnAlwaysFalseIfCriteriaKeyNotSupported() {
        var criteria =
            new SelectorCriteria("unsupported-field", Operator.Equals, Set.of("value1", "value2"));
        var predicate = converter.convert(criteria);
        assertNotNull(predicate);

        assertFalse(predicate.test(mock(Extension.class)));
    }
}
