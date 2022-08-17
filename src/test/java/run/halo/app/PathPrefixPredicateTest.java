package run.halo.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Test
    void test() {
        Flux.fromIterable(List.of(1, 2, 3))
            .flatMap(i -> {
                if (i == 2) {
                    return Mono.empty();
                }
                return Mono.just(i);
            }).subscribe(System.out::println);
    }
}
