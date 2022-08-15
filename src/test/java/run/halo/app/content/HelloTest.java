package run.halo.app.content;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class HelloTest {

    @Test
    void test() {
        Mono.just("C")
            .flatMap(c -> {
                Demo zhangsan = new Demo("zhangsan");
                return Mono.zip(Mono.just(zhangsan), hello(), hi());
            })
            .subscribe(tuple -> {
                System.out.println(tuple.getT1());
                System.out.println(tuple.getT2());
                System.out.println(tuple.getT3());
            });
    }

    Mono<String> hello() {
        return Mono.just("A");
    }

    Mono<String> hi() {
        return Mono.just("B");
    }

    record Demo(String name){}
}
