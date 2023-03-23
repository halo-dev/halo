package run.halo.app.extension.router.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static run.halo.app.extension.router.selector.Operator.Equals;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class SelectorConverterTest {

    SelectorConverter converter = new SelectorConverter();

    @Test
    void shouldConvertCorrectly() {
        record TestCase(String selector, SelectorCriteria expected) {
        }

        List.of(
            new TestCase("", null),
            new TestCase("name=value",
                new SelectorCriteria("name", Equals, Set.of("value"))),
            new TestCase("name!=value",
                new SelectorCriteria("name", Operator.NotEquals, Set.of("value"))),
            new TestCase("name",
                new SelectorCriteria("name", Operator.Exist, Set.of())),
            new TestCase("!name",
                new SelectorCriteria("name", Operator.NotExist, Set.of())),
            new TestCase("name",
                new SelectorCriteria("name", Operator.Exist, Set.of())),
            new TestCase("name!=",
                new SelectorCriteria("name!=", Operator.Exist, Set.of())),
            new TestCase("==",
                new SelectorCriteria("==", Operator.Exist, Set.of()))
        ).forEach(testCase -> {
            log.debug("Testing: {}", testCase);
            assertEquals(testCase.expected, converter.convert(testCase.selector));
        });
    }
}