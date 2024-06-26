package run.halo.app.core.extension.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import run.halo.app.extension.Metadata;

class PostTest {

    @ParameterizedTest
    @MethodSource("isRecycledProvider")
    void isRecycledTest(Metadata metadata, boolean expected) {
        assertEquals(expected, Post.isRecycled(metadata));
    }

    static Stream<Arguments> isRecycledProvider() {
        Function<Consumer<Metadata>, Metadata> metadataCreator =
            metadataConsumer -> {
                var metadata = new Metadata();
                metadataConsumer.accept(metadata);
                return metadata;
            };
        return Stream.of(
            Arguments.of(metadataCreator.apply(metadata -> {
            }), false),
            Arguments.of(metadataCreator.apply(metadata ->
                    metadata.setLabels(Map.of(Post.DELETED_LABEL, "false"))),
                false),
            Arguments.of(metadataCreator.apply(metadata ->
                    metadata.setLabels(Map.of(Post.DELETED_LABEL, "invalid"))),
                false),
            Arguments.of(metadataCreator.apply(metadata -> {
                metadata.setLabels(Map.of(Post.DELETED_LABEL, "true"));
            }), true)
        );

    }

}
