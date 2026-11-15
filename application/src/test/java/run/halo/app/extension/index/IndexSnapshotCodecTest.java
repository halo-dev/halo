package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.Test;

class IndexSnapshotCodecTest {

    @Test
    void truncatedLargeCountsMustNotExhaustHeap() throws Exception {
        var javaExecutable = java.nio.file.Path.of(System.getProperty("java.home"), "bin", "java")
                .toString();
        var classpath = java.util.stream.Stream.of(IndexSnapshotCodec.class, SnapshotCountProbe.class)
                .map(type ->
                        type.getProtectionDomain().getCodeSource().getLocation().getPath())
                .collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator));
        var process = new ProcessBuilder(
                        javaExecutable, "-Xmx32m", "-cp", classpath, SnapshotCountProbe.class.getName())
                .redirectErrorStream(true)
                .start();
        try {
            assertThat(process.waitFor(20, java.util.concurrent.TimeUnit.SECONDS))
                    .isTrue();
            var output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            assertThat(process.exitValue()).as(output).isZero();
            assertThat(output).contains("Rejected all five truncated counts");
        } finally {
            process.destroyForcibly();
        }
    }

    @Test
    void shouldRoundTrip() throws Exception {
        var snapshot = new IndicesSnapshot(List.of(
                new IndexSnapshot(
                        "spec.slug",
                        "fp-1",
                        "java.lang.String",
                        List.of(
                                new IndexSnapshot.Entry(List.of("hello"), List.of("p1", "p2")),
                                new IndexSnapshot.Entry(List.of("world"), List.of("p3"))),
                        List.of("p4")),
                new IndexSnapshot(
                        "metadata.labels",
                        "label-fp",
                        "label",
                        List.of(new IndexSnapshot.Entry(List.of("app", "halo"), List.of("p1"))),
                        List.of())));

        var out = new ByteArrayOutputStream();
        IndexSnapshotCodec.write(snapshot, out);
        var restored = IndexSnapshotCodec.read(new ByteArrayInputStream(out.toByteArray()));

        assertThat(restored).isEqualTo(snapshot);
    }

    @Test
    void shouldRoundTripLongTextKeys() throws Exception {
        for (var text : List.of("a".repeat(65536), "评论😀".repeat(30000))) {
            var snapshot = new IndicesSnapshot(List.of(new IndexSnapshot(
                    "text",
                    "fp",
                    "java.lang.String",
                    List.of(new IndexSnapshot.Entry(List.of(text), List.of("one"))),
                    List.of())));
            var out = new ByteArrayOutputStream();
            IndexSnapshotCodec.write(snapshot, out);
            assertThat(IndexSnapshotCodec.read(new ByteArrayInputStream(out.toByteArray())))
                    .isEqualTo(snapshot);
        }
    }

    @Test
    void shouldRejectBadMagic() {
        var garbage = new byte[] {0x1f, (byte) 0x8b, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(garbage)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }

    @Test
    void shouldRejectTruncatedStream() throws Exception {
        var snapshot = new IndicesSnapshot("checkpoint", List.of());
        var out = new ByteArrayOutputStream();
        IndexSnapshotCodec.write(snapshot, out);
        var truncated = new byte[out.size() / 2];
        System.arraycopy(out.toByteArray(), 0, truncated, 0, truncated.length);
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(truncated)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }

    @Test
    void shouldValidateGzipTrailer() throws Exception {
        var out = new ByteArrayOutputStream();
        IndexSnapshotCodec.write(new IndicesSnapshot("checkpoint", List.of()), out);
        var bytes = out.toByteArray();
        bytes[bytes.length - 1] ^= 1;
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(bytes)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }

    @Test
    void shouldRejectNegativeCount() throws Exception {
        var crafted = craftedStreamWithIndexCount(-1);
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(crafted)))
                .isInstanceOf(IndexSnapshotCorruptedException.class)
                .hasMessageContaining("Invalid count: -1");
    }

    @Test
    void shouldRejectExcessiveCount() throws Exception {
        var crafted = craftedStreamWithIndexCount(Integer.MAX_VALUE);
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(crafted)))
                .isInstanceOf(IndexSnapshotCorruptedException.class)
                .hasMessageContaining("Invalid count: " + Integer.MAX_VALUE);
    }

    @Test
    void shouldRejectInvalidStringLengthsAndTruncatedText() throws Exception {
        for (var length : List.of(-1, Integer.MAX_VALUE, 10)) {
            var bytes = stringPayload(3, length, new byte[0]);
            assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(bytes)))
                    .isInstanceOf(IndexSnapshotCorruptedException.class);
        }
    }

    @Test
    void shouldRejectInvalidUtf8InsteadOfChangingKeys() throws Exception {
        var bytes = stringPayload(3, 1, new byte[] {(byte) 0xff});
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(bytes)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }

    @Test
    void shouldRejectUnpairedSurrogatesInsteadOfChangingKeys() {
        var snapshot = new IndicesSnapshot("\ud800", List.of());
        assertThatThrownBy(() -> IndexSnapshotCodec.write(snapshot, new ByteArrayOutputStream()))
                .isInstanceOf(java.nio.charset.CharacterCodingException.class);
    }

    @Test
    void shouldRejectPreviousFormat() throws Exception {
        var bytes = stringPayload(2, 0, new byte[0]);
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(bytes)))
                .isInstanceOf(IndexSnapshotCorruptedException.class)
                .hasMessageContaining("Unsupported format version");
    }

    private static byte[] stringPayload(int format, int length, byte[] value) throws IOException {
        var out = new ByteArrayOutputStream();
        try (var data = new DataOutputStream(new GZIPOutputStream(out))) {
            data.writeInt(0x48414953);
            data.writeInt(format);
            data.writeInt(length);
            data.write(value);
        }
        return out.toByteArray();
    }

    /** Writes a valid GZIP stream with the magic and format version, followed by the given index count. */
    static byte[] craftedStreamWithIndexCount(int indexCount) throws IOException {
        var out = new ByteArrayOutputStream();
        try (var data = new DataOutputStream(new GZIPOutputStream(out))) {
            data.writeInt(0x48414953); // magic 'HAIS'
            data.writeInt(3); // format version
            data.writeInt(10);
            data.writeBytes("checkpoint");
            data.writeInt(indexCount);
        }
        return out.toByteArray();
    }
}

/** Standalone small-heap probe: depends only on the production codec and the JDK. */
class SnapshotCountProbe {
    public static void main(String[] args) throws Exception {
        for (int field = 0; field < 5; field++) {
            var bytes = new ByteArrayOutputStream();
            try (var data = new DataOutputStream(new GZIPOutputStream(bytes))) {
                data.writeInt(0x48414953);
                data.writeInt(3);
                data.writeInt(0); // checkpoint
                if (field > 0) {
                    data.writeInt(1); // index count
                    data.writeInt(0); // name
                    data.writeInt(0); // fingerprint
                    data.writeInt(0); // key type
                }
                if (field > 1) {
                    data.writeInt(0); // null keys
                }
                if (field > 2) {
                    data.writeInt(1); // entries
                }
                if (field > 3) {
                    data.writeInt(0); // key parts
                }
                data.writeInt(1 << 24);
            }
            try {
                IndexSnapshotCodec.read(new ByteArrayInputStream(bytes.toByteArray()));
                throw new AssertionError("Accepted truncated count " + field);
            } catch (IndexSnapshotCorruptedException expected) {
                // No array may be preallocated from an untrusted count before reading its contents.
            }
        }
        System.out.println("Rejected all five truncated counts");
    }
}
