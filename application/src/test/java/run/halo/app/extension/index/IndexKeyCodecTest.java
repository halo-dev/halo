package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class IndexKeyCodecTest {

    @Test
    void shouldRoundTripSupportedTypes() {
        assertThat(IndexKeyCodec.decode(String.class, IndexKeyCodec.encode("hello")))
                .isEqualTo("hello");
        assertThat(IndexKeyCodec.decode(Long.class, IndexKeyCodec.encode(42L))).isEqualTo(42L);
        assertThat(IndexKeyCodec.decode(Integer.class, IndexKeyCodec.encode(7))).isEqualTo(7);
        var now = Instant.now();
        assertThat(IndexKeyCodec.decode(Instant.class, IndexKeyCodec.encode(now)))
                .isEqualTo(now);
        assertThat(IndexKeyCodec.decode(Boolean.class, IndexKeyCodec.encode(true)))
                .isEqualTo(true);
    }

    @Test
    void shouldTreatUnknownKeyAsString() {
        var unknown = new UnknownKey("anything");
        assertThat(IndexKeyCodec.decode(UnknownKey.class, IndexKeyCodec.encode(unknown)))
                .isEqualTo(unknown);
    }

    @Test
    void shouldRoundTripEnumType() {
        assertThat(IndexKeyCodec.decode(TestVisibility.class, IndexKeyCodec.encode(TestVisibility.PUBLIC)))
                .isEqualTo(TestVisibility.PUBLIC);
        assertThat(IndexKeyCodec.decode(TestVisibility.class, IndexKeyCodec.encode(TestVisibility.PRIVATE)))
                .isEqualTo(TestVisibility.PRIVATE);
    }

    enum TestVisibility {
        PUBLIC,
        PRIVATE
    }

    @Test
    void shouldRejectUnsupportedType() {
        assertThatThrownBy(() -> IndexKeyCodec.decode(java.math.BigInteger.class, "42"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported index key type");
    }
}
