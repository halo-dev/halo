package run.halo.app.extension.router.selector;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.FakeExtension;
import run.halo.app.extension.Metadata;

class LabelCriteriaPredicateConverterTest {

    LabelCriteriaPredicateConverter<Extension> convert;

    @BeforeEach
    void setUp() {
        convert = new LabelCriteriaPredicateConverter<>();
    }

    @Test
    void shouldConvertEqualsCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.Equals, Set.of("value1", "value2"));
        var predicate = convert.convert(criteria);
        assertNotNull(predicate);
        var fakeExt = new FakeExtension();
        var metadata = new Metadata();
        fakeExt.setMetadata(metadata);
        assertFalse(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value"));
        assertFalse(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value1"));
        assertTrue(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value2"));
        assertTrue(predicate.test(fakeExt));
    }

    @Test
    void shouldConvertNotEqualsCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.NotEquals, Set.of("value1", "value2"));
        var predicate = convert.convert(criteria);
        assertNotNull(predicate);

        var fakeExt = new FakeExtension();
        var metadata = new Metadata();
        fakeExt.setMetadata(metadata);
        assertFalse(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value"));
        assertTrue(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value1"));
        assertFalse(predicate.test(fakeExt));

        metadata.setLabels(Map.of("name", "value2"));
        assertFalse(predicate.test(fakeExt));
    }

    @Test
    void shouldConvertNotExistCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.NotExist, Set.of());
        var predicate = convert.convert(criteria);
        assertNotNull(predicate);

        var fake = new FakeExtension();
        var metadata = new Metadata();
        fake.setMetadata(metadata);
        assertTrue(predicate.test(fake));

        metadata.setLabels(Map.of("not-a-name", ""));
        assertTrue(predicate.test(fake));

        metadata.setLabels(Map.of("name", ""));
        assertFalse(predicate.test(fake));
    }

    @Test
    void shouldConvertExistCorrectly() {
        var criteria = new SelectorCriteria("name", Operator.Exist, Set.of());
        var predicate = convert.convert(criteria);
        assertNotNull(predicate);

        var fake = new FakeExtension();
        var metadata = new Metadata();
        fake.setMetadata(metadata);
        assertFalse(predicate.test(fake));

        metadata.setLabels(Map.of("not-a-name", ""));
        assertFalse(predicate.test(fake));

        metadata.setLabels(Map.of("name", ""));
        assertTrue(predicate.test(fake));
    }

}