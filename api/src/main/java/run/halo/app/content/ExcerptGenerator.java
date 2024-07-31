package run.halo.app.content;

import java.util.Set;
import lombok.Data;
import lombok.experimental.Accessors;
import org.pf4j.ExtensionPoint;
import reactor.core.publisher.Mono;

public interface ExcerptGenerator extends ExtensionPoint {

    Mono<String> generate(ExcerptGenerator.Context context);

    @Data
    @Accessors(chain = true)
    class Context {
        private String raw;
        /**
         * html content.
         */
        private String content;

        private String rawType;
        /**
         * keywords in the content to help the excerpt generation more accurate.
         */
        private Set<String> keywords;
        /**
         * Max length of the generated excerpt.
         */
        private int maxLength;
    }
}
