package run.halo.app.extension.router.selector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static run.halo.app.extension.router.selector.Operator.Equals;
import static run.halo.app.extension.router.selector.Operator.Exist;
import static run.halo.app.extension.router.selector.Operator.IN;
import static run.halo.app.extension.router.selector.Operator.NotEquals;
import static run.halo.app.extension.router.selector.Operator.NotExist;

import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class OperatorTest {

    @Test
    void shouldConvertCorrectly() {
        record TestCase(String source, Operator converter, SelectorCriteria expected) {
        }

        List.of(
            new TestCase("", Equals, null),
            new TestCase("=", Equals, null),
            new TestCase("=value", Equals, null),
            new TestCase("name=", Equals, null),
            new TestCase("name=value", Equals,
                new SelectorCriteria("name", Equals, Set.of("value"))),

            new TestCase("", NotEquals, null),
            new TestCase("=", NotEquals, null),
            new TestCase("!", NotEquals, null),
            new TestCase("!=", NotEquals, null),
            new TestCase("!=value", NotEquals, null),
            new TestCase("name!=", NotEquals, null),
            new TestCase("name!=value", NotEquals,
                new SelectorCriteria("name", NotEquals, Set.of("value"))),

            new TestCase("", NotExist, null),
            new TestCase("!", NotExist, null),
            new TestCase("!name", NotExist, new SelectorCriteria("name", NotExist, Set.of())),
            new TestCase("name", NotExist, null),
            new TestCase("na!me", NotExist, null),
            new TestCase("name!", NotExist, null),

            new TestCase("name", Exist, new SelectorCriteria("name", Exist, Set.of())),
            new TestCase("", Exist, null),
            new TestCase("!", Exist, new SelectorCriteria("!", Exist, Set.of())),
            new TestCase("a", Exist, new SelectorCriteria("a", Exist, Set.of())),

            new TestCase("name", IN, null),
            new TestCase("name=(fake-name)", IN,
                new SelectorCriteria("name", IN, Set.of("fake-name"))),
            new TestCase("name=(first-name,second-name)", IN,
                new SelectorCriteria("name", IN, Set.of("first-name", "second-name")))
        ).forEach(testCase -> {
            log.debug("Testing: {}", testCase);
            assertEquals(testCase.expected(), testCase.converter().convert(testCase.source()));
        });
    }
}