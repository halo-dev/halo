package run.halo.app.extension.index;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Binary (GZIP-compressed) codec for {@link IndicesSnapshot}. Layout:
 *
 * <pre>
 * 4B magic 'HAIS' | 4B formatVersion
 * 4B indexCount | per index: UTF name, UTF fingerprint, UTF keyType,
 *     4B nullKeyCount + UTF*, 4B entryCount + per entry: 4B keyPartCount + UTF*, 4B pkCount + UTF*
 * 4B versionCount | per entry: UTF name, 8B version
 * </pre>
 *
 * @since 2.26.0
 */
final class IndexSnapshotCodec {

    private static final int MAGIC = 0x48414953; // 'HAIS'

    private static final int FORMAT_VERSION = 1;

    /** Upper bound for every count field; anything larger is treated as corruption instead of allocated. */
    private static final int MAX_COUNT = 1 << 24; // 16_777_216

    private IndexSnapshotCodec() {}

    static void write(IndicesSnapshot snapshot, OutputStream out) throws IOException {
        try (var data = new DataOutputStream(new GZIPOutputStream(out))) {
            data.writeInt(MAGIC);
            data.writeInt(FORMAT_VERSION);
            data.writeInt(snapshot.indices().size());
            for (var index : snapshot.indices()) {
                data.writeUTF(index.name());
                data.writeUTF(index.fingerprint());
                data.writeUTF(index.keyType());
                data.writeInt(index.nullKeys().size());
                for (var nullKey : index.nullKeys()) {
                    data.writeUTF(nullKey);
                }
                data.writeInt(index.entries().size());
                for (var entry : index.entries()) {
                    data.writeInt(entry.keyParts().size());
                    for (var part : entry.keyParts()) {
                        data.writeUTF(part);
                    }
                    data.writeInt(entry.primaryKeys().size());
                    for (var pk : entry.primaryKeys()) {
                        data.writeUTF(pk);
                    }
                }
            }
            data.writeInt(snapshot.versions().size());
            for (var versionEntry : snapshot.versions().entrySet()) {
                data.writeUTF(versionEntry.getKey());
                data.writeLong(versionEntry.getValue());
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
            var indexCount = readCount(data);
            var indices = new ArrayList<IndexSnapshot>(indexCount);
            for (var i = 0; i < indexCount; i++) {
                var name = data.readUTF();
                var fingerprint = data.readUTF();
                var keyType = data.readUTF();
                var nullKeyCount = readCount(data);
                var nullKeys = new ArrayList<String>(nullKeyCount);
                for (var j = 0; j < nullKeyCount; j++) {
                    nullKeys.add(data.readUTF());
                }
                var entryCount = readCount(data);
                var entries = new ArrayList<IndexSnapshot.Entry>(entryCount);
                for (var j = 0; j < entryCount; j++) {
                    var partCount = readCount(data);
                    var keyParts = new ArrayList<String>(partCount);
                    for (var k = 0; k < partCount; k++) {
                        keyParts.add(data.readUTF());
                    }
                    var pkCount = readCount(data);
                    var primaryKeys = new ArrayList<String>(pkCount);
                    for (var k = 0; k < pkCount; k++) {
                        primaryKeys.add(data.readUTF());
                    }
                    entries.add(new IndexSnapshot.Entry(List.copyOf(keyParts), List.copyOf(primaryKeys)));
                }
                indices.add(new IndexSnapshot(name, fingerprint, keyType, List.copyOf(entries), List.copyOf(nullKeys)));
            }
            var versionCount = readCount(data);
            var versions = new LinkedHashMap<String, Long>(versionCount);
            for (var i = 0; i < versionCount; i++) {
                versions.put(data.readUTF(), data.readLong());
            }
            return new IndicesSnapshot(List.copyOf(indices), Map.copyOf(versions));
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
