package run.halo.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;

/**
 * Test case for api path prefix predicate.
 *
 * @author guqing
 * @date 2022-04-13
 */
public class PathPrefixPredicateTest {

    @Test
    public void prefixPredicate() {
        boolean falseResult = HandlerTypePredicate.forAnnotation(RestController.class)
            .and(HandlerTypePredicate.forBasePackage(Application.class.getPackageName()))
            .test(getClass());
        assertThat(falseResult).isFalse();

        boolean result = HandlerTypePredicate.forAnnotation(RestController.class)
            .and(HandlerTypePredicate.forBasePackage(Application.class.getPackageName()))
            .test(TestController.class);
        assertThat(result).isTrue();
    }

    @RestController("controller-for-test")
    @RequestMapping("/test-prefix")
    class TestController {

    }

}
