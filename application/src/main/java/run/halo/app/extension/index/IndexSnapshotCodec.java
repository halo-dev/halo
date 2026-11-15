package run.halo.app.extension.index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Binary (GZIP-compressed) codec for {@link IndicesSnapshot}. Layout:
 *
 * <pre>
 * 4B magic 'HAIS' | 4B formatVersion | string checkpoint
 * 4B indexCount | per index: string name, string fingerprint, string keyType,
 *     4B nullKeyCount + string*, 4B entryCount + per entry: 4B keyPartCount + string*, 4B pkCount + string*
 * Strings use a 4B UTF-8 byte length followed by those bytes.
 * </pre>
 *
 * @since 2.26.0
 */
final class IndexSnapshotCodec {

    private static final int MAGIC = 0x48414953; // 'HAIS'

    private static final int FORMAT_VERSION = 3;

    /** Upper bound for every count field; anything larger is treated as corruption instead of allocated. */
    private static final int MAX_COUNT = 1 << 24; // 16_777_216

    // Enough for indexed request bodies while bounding allocation for malformed files.
    private static final int MAX_STRING_BYTES = 64 * 1024 * 1024;

    private IndexSnapshotCodec() {}

    private static void writeString(DataOutputStream data, String value) throws IOException {
        var bytes = StandardCharsets.UTF_8.newEncoder().encode(CharBuffer.wrap(value));
        if (bytes.remaining() > MAX_STRING_BYTES) {
            throw new IOException("Snapshot string exceeds " + MAX_STRING_BYTES + " bytes");
        }
        data.writeInt(bytes.remaining());
        data.write(bytes.array(), bytes.position(), bytes.remaining());
    }

    private static String readString(DataInputStream data) throws IOException {
        var length = data.readInt();
        if (length < 0 || length > MAX_STRING_BYTES) {
            throw new IndexSnapshotCorruptedException("Invalid string length: " + length);
        }
        var bytes = data.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("Truncated string");
        }
        return StandardCharsets.UTF_8
                .newDecoder()
                .decode(ByteBuffer.wrap(bytes))
                .toString();
    }

    static void write(IndicesSnapshot snapshot, OutputStream out) throws IOException {
        try (var data = new DataOutputStream(new GZIPOutputStream(out))) {
            data.writeInt(MAGIC);
            data.writeInt(FORMAT_VERSION);
            writeString(data, snapshot.checkpoint());
            data.writeInt(snapshot.indices().size());
            for (var index : snapshot.indices()) {
                writeString(data, index.name());
                writeString(data, index.fingerprint());
                writeString(data, index.keyType());
                data.writeInt(index.nullKeys().size());
                for (var nullKey : index.nullKeys()) {
                    writeString(data, nullKey);
                }
                data.writeInt(index.entries().size());
                for (var entry : index.entries()) {
                    data.writeInt(entry.keyParts().size());
                    for (var part : entry.keyParts()) {
                        writeString(data, part);
                    }
                    data.writeInt(entry.primaryKeys().size());
                    for (var pk : entry.primaryKeys()) {
                        writeString(data, pk);
                    }
                }
            }
        }
    }

    static IndicesSnapshot read(InputStream in) throws IndexSnapshotCorruptedException {
        try (var data = new DataInputStream(new GZIPInputStream(in))) {
            if (data.readInt() != MAGIC) {
                throw new IndexSnapshotCorruptedException("Bad magic");
            }
            if (data.readInt() != FORMAT_VERSION) {
                throw new IndexSnapshotCorruptedException("Unsupported format version");
            }
            var checkpoint = readString(data);
            var indexCount = readCount(data);
            // Counts are untrusted: grow with decoded data instead of allocating from the header.
            var indices = new ArrayList<IndexSnapshot>(Math.min(indexCount, 1024));
            for (var i = 0; i < indexCount; i++) {
                var name = readString(data);
                var fingerprint = readString(data);
                var keyType = readString(data);
                var nullKeyCount = readCount(data);
                var nullKeys = new ArrayList<String>(Math.min(nullKeyCount, 1024));
                for (var j = 0; j < nullKeyCount; j++) {
                    nullKeys.add(readString(data));
                }
                var entryCount = readCount(data);
                var entries = new ArrayList<IndexSnapshot.Entry>(Math.min(entryCount, 1024));
                for (var j = 0; j < entryCount; j++) {
                    var partCount = readCount(data);
                    var keyParts = new ArrayList<String>(Math.min(partCount, 1024));
                    for (var k = 0; k < partCount; k++) {
                        keyParts.add(readString(data));
                    }
                    var pkCount = readCount(data);
                    var primaryKeys = new ArrayList<String>(Math.min(pkCount, 1024));
                    for (var k = 0; k < pkCount; k++) {
                        primaryKeys.add(readString(data));
                    }
                    entries.add(new IndexSnapshot.Entry(List.copyOf(keyParts), List.copyOf(primaryKeys)));
                }
                indices.add(new IndexSnapshot(name, fingerprint, keyType, List.copyOf(entries), List.copyOf(nullKeys)));
            }
            // Read through the GZIP trailer to verify CRC and reject trailing payload.
            if (data.read() != -1) {
                throw new IndexSnapshotCorruptedException("Trailing snapshot data");
            }
            return new IndicesSnapshot(checkpoint, List.copyOf(indices));
        } catch (IndexSnapshotCorruptedException e) {
            throw e;
        } catch (EOFException e) {
            throw new IndexSnapshotCorruptedException("Truncated snapshot", e);
        } catch (IOException e) {
            throw new IndexSnapshotCorruptedException("Corrupted snapshot: " + e.getMessage(), e);
        }
    }

    private static int readCount(DataInputStream data) throws IOException {
        var count = data.readInt();
        if (count < 0 || count > MAX_COUNT) {
            throw new IndexSnapshotCorruptedException("Invalid count: " + count);
        }
        return count;
    }
}

/** Thrown when an index snapshot file is corrupted or has an unsupported format. */
class IndexSnapshotCorruptedException extends RuntimeException {

    IndexSnapshotCorruptedException(String message) {
        super(message);
    }

    IndexSnapshotCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
