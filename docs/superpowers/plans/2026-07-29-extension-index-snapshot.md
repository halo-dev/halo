# Extension 索引快照与差量恢复 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 Halo 的 Extension 内存索引可持久化：正常关闭/插件停止时保存快照，启动/插件启动时加载快照并差量恢复，同时修复 DB 事务与索引的提交点对齐问题。

**Architecture:** 索引快照以 GZIP 二进制文件存于 `<work-dir>/indices/<type>.snapshot.gz`，含逐索引指纹与 `name→version` 清单；恢复按指纹匹配分三级（不可用→全量构建 / 匹配→窄查询差量 / 有脏索引→合并全表扫描）。设计文档：`docs/superpowers/specs/2026-07-29-extension-index-snapshot-design.md`。

**Tech Stack:** Java 21, Spring Boot WebFlux + R2DBC, JUnit 5 + AssertJ + Mockito, Gradle。

## Global Constraints

- 所有命令从仓库根目录执行；后端测试用 `./gradlew :application:test --tests '<pattern>'`，API 模块用 `./gradlew :api:test --tests '<pattern>'`。
- 每个任务完成后运行 `./gradlew spotlessApply` 再提交（Palantir 格式，4 空格、120 列）。
- 零新增第三方依赖（只用 JDK + 现有 Spring/JUnit/Mockito/AssertJ）。
- `api/` 模块仅允许 additive 变更（新增方法/默认方法），不改既有签名。
- 新增类放在 `application/src/main/java/run/halo/app/extension/index/`（与 `SingleValueIndex` 等同包，api 模块有同名包是既有的 split-package 现状）。
- 不写 `System.out`；日志用 Lombok `@Slf4j`。
- 提交信息用 Conventional Commits（如 `feat: ...`）；`git add` 只暂存具体文件，禁止 `git add -A`。
- 设计文档中"超时时间可配置"简化为常量 `Duration.ofSeconds(30)`（YAGNI，后续需要再提配置项）。

---

### Task 1: api 模块 — IndexSpec 索引版本号

**Files:**
- Modify: `api/src/main/java/run/halo/app/extension/index/ValueIndexSpec.java`
- Modify: `api/src/main/java/run/halo/app/extension/index/IndexSpecBuilder.java`
- Modify: `api/src/main/java/run/halo/app/extension/index/AbstractValueIndexSpecBuilder.java`
- Modify: `api/src/main/java/run/halo/app/extension/index/SingleValueBuilder.java`
- Modify: `api/src/main/java/run/halo/app/extension/index/MultiValueBuilder.java`
- Test: `api/src/test/java/run/halo/app/extension/index/IndexSpecVersionTest.java`

**Interfaces:**
- Produces: `ValueIndexSpec#getVersion()`（default 返回 `1`）；`IndexSpecBuilder#version(int)`（fluent）。Task 2 的指纹计算与 Task 3 的快照依赖 `getVersion()`。

- [ ] **Step 1: Write the failing test**

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;

class IndexSpecVersionTest {

    @Test
    void defaultVersionShouldBeOne() {
        var spec = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName())
                .build();
        assertThat(spec.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldSetVersionOnSingleValueSpec() {
        var spec = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName())
                .version(3)
                .build();
        assertThat(spec.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldSetVersionOnMultiValueSpec() {
        var spec = IndexSpecs.<FakeExtension, String>multi("spec.tags", String.class)
                .indexFunc(e -> java.util.Set.of("a"))
                .version(2)
                .build();
        assertThat(spec.getVersion()).isEqualTo(2);
    }

    @Test
    void shouldRejectNonPositiveVersion() {
        var builder = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> e.getMetadata().getName());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> builder.version(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class FakeExtension implements Extension {
        private final Metadata metadata = new Metadata();

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :api:test --tests 'run.halo.app.extension.index.IndexSpecVersionTest'`
Expected: 编译失败，`getVersion()` / `version(int)` 不存在。

- [ ] **Step 3: Implement**

`ValueIndexSpec.java` 增加默认方法：

```java
/**
 * Gets the version of this index spec. Bump the version to trigger a rebuild of the index
 * when the semantics of the index function change without any structural change.
 *
 * @return the version of this index spec, defaults to 1
 * @since 2.23.0
 */
default int getVersion() {
    return 1;
}
```

`IndexSpecBuilder.java` 增加接口方法（放在 `nullable` 之后）：

```java
/**
 * Sets the version of the index. Bump it when the index function changes its semantics
 * so that persisted snapshots of this index are invalidated.
 *
 * @param version the index version, must be positive, default is 1
 * @return the updated IndexSpecBuilder
 */
B version(int version);
```

`AbstractValueIndexSpecBuilder.java` 增加字段与方法：

```java
protected int version = 1;
```

```java
@Override
public B version(int version) {
    Assert.isTrue(version > 0, "Index version must be positive");
    this.version = version;
    return (B) this;
}
```

`SingleValueBuilder.java` 的匿名 `SingleValueIndexSpec` 中增加：

```java
@Override
public int getVersion() {
    return version;
}
```

`MultiValueBuilder.java` 的匿名 `MultiValueIndexSpec` 中增加同样的 `getVersion()` 覆写。

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :api:test --tests 'run.halo.app.extension.index.IndexSpecVersionTest'`
Expected: PASS（4 个用例）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add api/src/main/java/run/halo/app/extension/index/ api/src/test/java/run/halo/app/extension/index/IndexSpecVersionTest.java
git commit -m "feat: add version attribute to index specs for snapshot invalidation"
```

---

### Task 2: 键编解码器与索引指纹工具

**Files:**
- Create: `application/src/main/java/run/halo/app/extension/index/IndexKeyCodec.java`
- Create: `application/src/main/java/run/halo/app/extension/index/IndexFingerprints.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndexKeyCodecTest.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndexFingerprintsTest.java`

**Interfaces:**
- Consumes: Task 1 的 `ValueIndexSpec#getVersion()`。
- Produces: `IndexKeyCodec.encode(Object) → String`、`IndexKeyCodec.decode(Class<K>, String) → K`；`IndexFingerprints.fingerprint(ValueIndexSpec<?,?>) → String`、`IndexFingerprints.LABEL_FINGERPRINT` 常量。Task 3/6/7 依赖。

- [ ] **Step 1: Write the failing test**

`IndexKeyCodecTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class IndexKeyCodecTest {

    @Test
    void shouldRoundTripSupportedTypes() {
        assertThat(IndexKeyCodec.decode(String.class, IndexKeyCodec.encode("hello"))).isEqualTo("hello");
        assertThat(IndexKeyCodec.decode(Long.class, IndexKeyCodec.encode(42L))).isEqualTo(42L);
        assertThat(IndexKeyCodec.decode(Integer.class, IndexKeyCodec.encode(7))).isEqualTo(7);
        var now = Instant.now();
        assertThat(IndexKeyCodec.decode(Instant.class, IndexKeyCodec.encode(now))).isEqualTo(now);
        assertThat(IndexKeyCodec.decode(Boolean.class, IndexKeyCodec.encode(true))).isEqualTo(true);
    }

    @Test
    void shouldTreatUnknownKeyAsString() {
        var unknown = new UnknownKey("anything");
        assertThat(IndexKeyCodec.decode(UnknownKey.class, IndexKeyCodec.encode(unknown)))
                .isEqualTo(unknown);
    }

    @Test
    void shouldRejectUnsupportedType() {
        assertThatThrownBy(() -> IndexKeyCodec.decode(java.time.LocalDate.class, "2026-01-01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported index key type");
    }
}
```

`IndexFingerprintsTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;

class IndexFingerprintsTest {

    @Test
    void sameSpecShouldProduceSameFingerprint() {
        var a = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x").unique(true).build();
        var b = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "y").unique(true).build();
        assertThat(IndexFingerprints.fingerprint(a))
                .isEqualTo(IndexFingerprints.fingerprint(b));
    }

    @Test
    void differentVersionShouldProduceDifferentFingerprint() {
        var v1 = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x").build();
        var v2 = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x").version(2).build();
        assertThat(IndexFingerprints.fingerprint(v1))
                .isNotEqualTo(IndexFingerprints.fingerprint(v2));
    }

    @Test
    void differentStructureShouldProduceDifferentFingerprint() {
        var base = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x").build();
        var unique = IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                .indexFunc(e -> "x").unique(true).build();
        var multi = IndexSpecs.<FakeExtension, String>multi("spec.slug", String.class)
                .indexFunc(e -> Set.of("x")).build();
        var otherKeyType = IndexSpecs.<FakeExtension, Long>single("spec.slug", Long.class)
                .indexFunc(e -> 1L).build();
        assertThat(IndexFingerprints.fingerprint(base))
                .isNotEqualTo(IndexFingerprints.fingerprint(unique))
                .isNotEqualTo(IndexFingerprints.fingerprint(multi))
                .isNotEqualTo(IndexFingerprints.fingerprint(otherKeyType));
    }

    static class FakeExtension implements Extension {
        private final Metadata metadata = new Metadata();

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

注：若 `UnknownKey` 不是公开构造或无可比较性，查看 `api/src/main/java/run/halo/app/extension/index/UnknownKey.java` 后按实际 API 调整该用例（例如改为只断言 encode 输出字符串）。

- [ ] **Step 2: Run tests to verify they fail**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexKeyCodecTest' --tests 'run.halo.app.extension.index.IndexFingerprintsTest'`
Expected: 编译失败，两个类不存在。

- [ ] **Step 3: Implement**

`IndexKeyCodec.java`：

```java
package run.halo.app.extension.index;

import java.time.Instant;

/**
 * Codec between index keys and their canonical string representation for snapshot persistence.
 * Supports the key types actually used by index specs; fails fast on unsupported types.
 *
 * @since 2.23.0
 */
final class IndexKeyCodec {

    private IndexKeyCodec() {}

    static String encode(Object key) {
        if (key instanceof Instant instant) {
            return instant.toString();
        }
        return key.toString();
    }

    @SuppressWarnings("unchecked")
    static <K extends Comparable<K>> K decode(Class<K> keyType, String text) {
        if (keyType == String.class) {
            return (K) text;
        }
        if (keyType == UnknownKey.class) {
            return (K) new UnknownKey(text);
        }
        if (keyType == Long.class) {
            return (K) Long.valueOf(text);
        }
        if (keyType == Integer.class) {
            return (K) Integer.valueOf(text);
        }
        if (keyType == Instant.class) {
            return (K) Instant.parse(text);
        }
        if (keyType == Boolean.class) {
            return (K) Boolean.valueOf(text);
        }
        throw new IllegalArgumentException("Unsupported index key type: " + keyType.getName());
    }
}
```

`IndexFingerprints.java`：

```java
package run.halo.app.extension.index;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Computes structural fingerprints of index specs. The fingerprint covers name, single/multi
 * kind, key type, unique, nullable and the spec version; it intentionally does NOT cover the
 * index function body, which is why {@link ValueIndexSpec#getVersion()} exists.
 *
 * @since 2.23.0
 */
public final class IndexFingerprints {

    /** Fingerprint of the built-in label index, which has no user-facing spec. */
    public static final String LABEL_FINGERPRINT = "metadata.labels|label|v1";

    private IndexFingerprints() {}

    public static String fingerprint(ValueIndexSpec<?, ?> spec) {
        var kind = spec instanceof MultiValueIndexSpec<?, ?> ? "multi" : "single";
        var canonical = String.join(
                "|",
                spec.getName(),
                kind,
                spec.getKeyType().getName(),
                Boolean.toString(spec.isUnique()),
                Boolean.toString(spec.isNullable()),
                Integer.toString(spec.getVersion()));
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
```

注：`UnknownKey` 若构造器签名不同，按 `api/src/main/java/run/halo/app/extension/index/UnknownKey.java` 实际定义调整。

- [ ] **Step 4: Run tests to verify they pass**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexKeyCodecTest' --tests 'run.halo.app.extension.index.IndexFingerprintsTest'`
Expected: PASS。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/IndexKeyCodec.java \
  application/src/main/java/run/halo/app/extension/index/IndexFingerprints.java \
  application/src/test/java/run/halo/app/extension/index/IndexKeyCodecTest.java \
  application/src/test/java/run/halo/app/extension/index/IndexFingerprintsTest.java
git commit -m "feat: add index key codec and spec fingerprint utils for index snapshots"
```

---

### Task 3: 快照数据模型与 Index 实现的 dump/restore

**Files:**
- Create: `application/src/main/java/run/halo/app/extension/index/IndexSnapshot.java`
- Create: `application/src/main/java/run/halo/app/extension/index/IndicesSnapshot.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/Index.java`（新增 3 个方法）
- Modify: `application/src/main/java/run/halo/app/extension/index/SingleValueIndex.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/MultiValueIndex.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/LabelIndex.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndexDumpRestoreTest.java`

**Interfaces:**
- Consumes: Task 2 的 `IndexKeyCodec`、`IndexFingerprints`（含 `LABEL_FINGERPRINT`）。
- Produces:
- `record IndexSnapshot(String name, String fingerprint, String keyType, List<Entry> entries, List<String> nullKeys)`，内嵌 `record Entry(List<String> keyParts, List<String> primaryKeys)`
- `record IndicesSnapshot(List<IndexSnapshot> indices, Map<String, Long> versions)`
- `Index` 新增：`IndexSnapshot dump()`、`void restore(IndexSnapshot snapshot)`、`String getFingerprint()`
- Task 4（DefaultIndices）与 Task 7（Codec）依赖以上签名。

- [ ] **Step 1: Write the failing test**

`IndexDumpRestoreTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class IndexDumpRestoreTest {

    @Test
    void singleValueIndexShouldRoundTrip() {
        var spec = IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                .indexFunc(p -> p.slug)
                .unique(true)
                .nullable(false)
                .build();
        var index = new SingleValueIndex<>(spec);
        index.insert(new FakePost("post-1", "hello", Instant.parse("2026-01-01T00:00:00Z")));
        index.insert(new FakePost("post-2", "world", Instant.parse("2026-01-02T00:00:00Z")));

        var snapshot = index.dump();
        assertThat(snapshot.name()).isEqualTo("spec.slug");
        assertThat(snapshot.fingerprint()).isEqualTo(IndexFingerprints.fingerprint(spec));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(snapshot);
        assertThat(restored.equal("hello")).containsExactly("post-1");
        assertThat(restored.equal("world")).containsExactly("post-2");
        assertThat(restored.getKey("post-2")).isEqualTo("world");
    }

    @Test
    void singleValueIndexWithInstantKeyShouldRoundTrip() {
        var spec = IndexSpecs.<FakePost, Instant>single("spec.publishTime", Instant.class)
                .indexFunc(p -> p.publishTime)
                .build();
        var index = new SingleValueIndex<>(spec);
        var t = Instant.parse("2026-06-01T12:00:00Z");
        index.insert(new FakePost("post-1", "a", t));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.equal(t)).containsExactly("post-1");
    }

    @Test
    void multiValueIndexShouldRoundTrip() {
        var spec = IndexSpecs.<FakePost, String>multi("spec.tags", String.class)
                .indexFunc(p -> p.tags)
                .build();
        var index = new MultiValueIndex<>(spec);
        var post = new FakePost("post-1", "a", null);
        post.tags = Set.of("java", "halo");
        index.insert(post);

        var restored = new MultiValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.equal("java")).containsExactly("post-1");
        assertThat(restored.getKeys("post-1")).containsExactlyInAnyOrder("java", "halo");
    }

    @Test
    void labelIndexShouldRoundTrip() {
        var index = new LabelIndex<FakePost>();
        var post = new FakePost("post-1", "a", null);
        post.getMetadata().setLabels(Map.of("app", "halo", "env", "prod"));
        index.insert(post);

        var restored = new LabelIndex<FakePost>();
        var snapshot = index.dump();
        assertThat(snapshot.fingerprint()).isEqualTo(IndexFingerprints.LABEL_FINGERPRINT);
        restored.restore(snapshot);
        assertThat(restored.equal("app", "halo")).containsExactly("post-1");
        assertThat(restored.exists("env")).containsExactly("post-1");
    }

    @Test
    void nullKeysShouldRoundTrip() {
        var spec = IndexSpecs.<FakePost, Instant>single("spec.publishTime", Instant.class)
                .indexFunc(p -> p.publishTime)
                .nullable(true)
                .build();
        var index = new SingleValueIndex<>(spec);
        index.insert(new FakePost("post-1", "a", null));

        var restored = new SingleValueIndex<>(spec);
        restored.restore(index.dump());
        assertThat(restored.isNull()).containsExactly("post-1");
    }

    static class FakePost implements run.halo.app.extension.Extension {
        private final run.halo.app.extension.Metadata metadata = new run.halo.app.extension.Metadata();
        String slug;
        Instant publishTime;
        Set<String> tags = Set.of();

        FakePost(String name, String slug, Instant publishTime) {
            metadata.setName(name);
            this.slug = slug;
            this.publishTime = publishTime;
        }

        @Override
        public run.halo.app.extension.Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(run.halo.app.extension.Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexDumpRestoreTest'`
Expected: 编译失败，`IndexSnapshot`/`dump`/`restore` 不存在。

- [ ] **Step 3: Implement**

`IndexSnapshot.java`：

```java
package run.halo.app.extension.index;

import java.util.List;

/**
 * Serializable snapshot of a single index. Keys are stored as canonical string parts
 * (one part for value indexes, two parts {@code [labelKey, labelValue]} for the label index).
 *
 * @since 2.23.0
 */
public record IndexSnapshot(
        String name, String fingerprint, String keyType, List<Entry> entries, List<String> nullKeys) {

    public record Entry(List<String> keyParts, List<String> primaryKeys) {}
}
```

`IndicesSnapshot.java`：

```java
package run.halo.app.extension.index;

import java.util.List;
import java.util.Map;

/**
 * Snapshot of all indices of one extension type, plus the {@code name -> version} manifest
 * used as the watermark for delta recovery.
 *
 * @since 2.23.0
 */
public record IndicesSnapshot(List<IndexSnapshot> indices, Map<String, Long> versions) {}
```

`Index.java` 增加三个方法（接口方法，非 default）：

```java
    /**
     * Dumps the content of this index into a serializable snapshot.
     *
     * @return the snapshot of this index
     * @since 2.23.0
     */
    IndexSnapshot dump();

    /**
     * Restores the content of this index from a snapshot. The index must be empty.
     *
     * @param snapshot the snapshot to restore from
     * @since 2.23.0
     */
    void restore(IndexSnapshot snapshot);

    /**
     * Gets the structural fingerprint of this index, used to match snapshots against specs.
     *
     * @return the fingerprint
     * @since 2.23.0
     */
    String getFingerprint();
```

`SingleValueIndex.java` 增加：

```java
    @Override
    public IndexSnapshot dump() {
        var entries = index.entrySet().stream()
                .map(e -> new IndexSnapshot.Entry(
                        List.of(IndexKeyCodec.encode(e.getKey())), List.copyOf(e.getValue())))
                .toList();
        return new IndexSnapshot(
                getName(), getFingerprint(), getKeyType().getName(), entries, List.copyOf(nullKeyValues));
    }

    @Override
    public void restore(IndexSnapshot snapshot) {
        for (var entry : snapshot.entries()) {
            K key = IndexKeyCodec.decode(getKeyType(), entry.keyParts().getFirst());
            var primaryKeys = ConcurrentHashMap.<String>newKeySet();
            primaryKeys.addAll(entry.primaryKeys());
            index.put(key, primaryKeys);
            entry.primaryKeys().forEach(pk -> invertedIndex.put(pk, key));
        }
        nullKeyValues.addAll(snapshot.nullKeys());
    }

    @Override
    public String getFingerprint() {
        return IndexFingerprints.fingerprint(spec);
    }
```

`MultiValueIndex.java` 增加（反向表是 `Map<String, Set<K>>`，需按主键聚合）：

```java
    @Override
    public IndexSnapshot dump() {
        var entries = index.entrySet().stream()
                .map(e -> new IndexSnapshot.Entry(
                        List.of(IndexKeyCodec.encode(e.getKey())), List.copyOf(e.getValue())))
                .toList();
        return new IndexSnapshot(
                getName(), getFingerprint(), getKeyType().getName(), entries, List.copyOf(nullKeyValues));
    }

    @Override
    public void restore(IndexSnapshot snapshot) {
        for (var entry : snapshot.entries()) {
            K key = IndexKeyCodec.decode(getKeyType(), entry.keyParts().getFirst());
            var primaryKeys = ConcurrentHashMap.<String>newKeySet();
            primaryKeys.addAll(entry.primaryKeys());
            index.put(key, primaryKeys);
            entry.primaryKeys().forEach(pk -> invertedIndex
                    .computeIfAbsent(pk, k -> ConcurrentHashMap.newKeySet())
                    .add(key));
        }
        nullKeyValues.addAll(snapshot.nullKeys());
    }

    @Override
    public String getFingerprint() {
        return IndexFingerprints.fingerprint(spec);
    }
```

`LabelIndex.java` 增加：

```java
    @Override
    public IndexSnapshot dump() {
        var entries = index.entrySet().stream()
                .map(e -> new IndexSnapshot.Entry(
                        List.of(e.getKey().labelKey(), e.getKey().labelValue()),
                        List.copyOf(e.getValue())))
                .toList();
        return new IndexSnapshot(getName(), getFingerprint(), "label", entries, List.of());
    }

    @Override
    public void restore(IndexSnapshot snapshot) {
        for (var entry : snapshot.entries()) {
            var labelEntry = new LabelEntry(entry.keyParts().get(0), entry.keyParts().get(1));
            var primaryKeys = ConcurrentHashMap.<String>newKeySet();
            primaryKeys.addAll(entry.primaryKeys());
            index.put(labelEntry, primaryKeys);
            entry.primaryKeys().forEach(pk -> invertedIndex
                    .computeIfAbsent(pk, k -> ConcurrentHashMap.newKeySet())
                    .add(labelEntry));
        }
    }

    @Override
    public String getFingerprint() {
        return IndexFingerprints.LABEL_FINGERPRINT;
    }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexDumpRestoreTest'`
Expected: PASS（5 个用例）。同时运行 `./gradlew :application:test --tests 'run.halo.app.extension.index.*'` 确认既有测试不破。

注意：`Index` 接口新增 3 个抽象方法后，除 `SingleValueIndex`/`MultiValueIndex`/`LabelIndex`
外若还有其他实现（包括测试里的 fake/mock 实现类），编译会报错——用
`grep -rn "implements.*Index<" application/src` 排查，测试 fake 补空实现（`dump` 返回
`null` 或 `throw new UnsupportedOperationException()`）即可。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/ application/src/test/java/run/halo/app/extension/index/IndexDumpRestoreTest.java
git commit -m "feat: add dump and restore capabilities to index implementations"
```

---

### Task 4: DefaultIndices — version 映射、版本守卫、deleteByName、部分索引更新、聚合 dump/restore

**Files:**
- Modify: `application/src/main/java/run/halo/app/extension/index/Indices.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/DefaultIndices.java`
- Test: `application/src/test/java/run/halo/app/extension/index/DefaultIndicesSnapshotTest.java`

**Interfaces:**
- Consumes: Task 3 的 `IndicesSnapshot`/`IndexSnapshot`、`Index#dump/restore/getFingerprint`。
- Produces（Task 7/8/9/10 依赖）：
- `Indices#dump() → IndicesSnapshot`（内部顺序：**先复制 version 映射，再 dump 各索引**）
- `Indices#restore(IndicesSnapshot)`（按名称匹配灌入索引 + 加载 version 映射）
- `Indices#deleteByName(String primaryKey)`
- `Indices#updateIndices(E extension, Set<String> indexNames)`（只对指定索引做 upsert）
- `Indices#currentFingerprints() → Map<String, String>`
- 版本守卫：`update` 时 incoming version < 已记录 version → 跳过；version 映射在索引条目提交**之后**更新。

- [ ] **Step 1: Write the failing test**

`DefaultIndicesSnapshotTest.java`（复用 Task 3 测试里的 `FakePost` 结构，复制到本文件）：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DefaultIndicesSnapshotTest {

    private static DefaultIndices<FakePost> newIndices() {
        var slugSpec = IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                .indexFunc(p -> p.slug)
                .nullable(false)
                .build();
        var tagsSpec = IndexSpecs.<FakePost, String>multi("spec.tags", String.class)
                .indexFunc(p -> p.tags)
                .build();
        return new DefaultIndices<>(List.of(
                new SingleValueIndex<>(slugSpec), new MultiValueIndex<>(tagsSpec), new LabelIndex<>()));
    }

    @Test
    void dumpAndRestoreShouldRoundTrip() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(5L);
        post.tags = Set.of("java");
        indices.insert(post);

        var snapshot = indices.dump();
        assertThat(snapshot.versions()).containsEntry("post-1", 5L);

        var restored = DefaultIndicesSnapshotTest.<FakePost>newIndices();
        restored.restore(snapshot);
        assertThat(restored.<String>getIndex("spec.slug").equal("hello")).containsExactly("post-1");
        assertThat(restored.<String>getIndex("spec.tags").equal("java")).containsExactly("post-1");
        assertThat(restored.dump().versions()).containsEntry("post-1", 5L);
    }

    @Test
    void staleUpdateShouldBeSkippedByVersionGuard() {
        var indices = newIndices();
        var v2 = new FakePost("post-1", "new", null);
        v2.getMetadata().setVersion(2L);
        indices.insert(v2);

        var v1 = new FakePost("post-1", "old", null);
        v1.getMetadata().setVersion(1L);
        indices.update(v1);

        assertThat(indices.<String>getIndex("spec.slug").equal("new")).containsExactly("post-1");
        assertThat(indices.<String>getIndex("spec.slug").equal("old")).isEmpty();
    }

    @Test
    void deleteByNameShouldRemoveAllIndexEntries() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(1L);
        post.tags = Set.of("java");
        indices.insert(post);

        indices.deleteByName("post-1");

        assertThat(indices.<String>getIndex("spec.slug").equal("hello")).isEmpty();
        assertThat(indices.<String>getIndex("spec.tags").equal("java")).isEmpty();
        assertThat(indices.dump().versions()).doesNotContainKey("post-1");
    }

    @Test
    void updateIndicesShouldOnlyTouchNamedIndices() {
        var indices = newIndices();
        var post = new FakePost("post-1", "hello", null);
        post.getMetadata().setVersion(1L);
        indices.updateIndices(post, Set.of("spec.slug"));

        assertThat(indices.<String>getIndex("spec.slug").equal("hello")).containsExactly("post-1");
        assertThat(indices.dump().versions()).containsEntry("post-1", 1L);
        // tags index was not updated
        post.tags = Set.of("java");
        indices.updateIndices(post, Set.of("spec.tags"));
        assertThat(indices.<String>getIndex("spec.tags").equal("java")).containsExactly("post-1");
    }

    @Test
    void currentFingerprintsShouldCoverAllIndices() {
        var indices = newIndices();
        assertThat(indices.currentFingerprints())
                .containsKeys("spec.slug", "spec.tags", "metadata.labels");
    }

    static class FakePost implements run.halo.app.extension.Extension {
        private final run.halo.app.extension.Metadata metadata = new run.halo.app.extension.Metadata();
        String slug;
        Set<String> tags = Set.of();

        FakePost(String name, String slug, Instant publishTime) {
            metadata.setName(name);
            this.slug = slug;
        }

        @Override
        public run.halo.app.extension.Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(run.halo.app.extension.Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

注：`new DefaultIndices<>(...)` 与 `DefaultIndices` 构造器为包级可见，测试在同包下可直接调用。若 `updateIndices` 语义定为"不在 version 映射中的行也写入 version 映射"，按上面断言；实现时保持测试与实现一致。

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.DefaultIndicesSnapshotTest'`
Expected: 编译失败。

- [ ] **Step 3: Implement**

`Indices.java` 增加：

```java
    /**
     * Deletes all index entries for the given primary key.
     *
     * @param primaryKey the primary key
     */
    void deleteByName(String primaryKey);

    /**
     * Updates only the named indices with the given extension (upsert semantics).
     *
     * @param extension the extension
     * @param indexNames the names of the indices to update
     */
    void updateIndices(E extension, java.util.Set<String> indexNames);

    /**
     * Dumps all indices and the name-to-version manifest into a snapshot. The manifest is
     * captured BEFORE the indices so that a torn dump always errs on the safe side
     * (manifest version <= index content); see the design doc section 6.3.
     *
     * @return the snapshot
     */
    IndicesSnapshot dump();

    /**
     * Restores indices (matched by name) and the manifest from a snapshot.
     *
     * @param snapshot the snapshot; indices not present here are left untouched (empty)
     */
    void restore(IndicesSnapshot snapshot);

    /**
     * Gets the current fingerprint of each index, keyed by index name.
     *
     * @return map of index name to fingerprint
     */
    java.util.Map<String, String> currentFingerprints();
```

`DefaultIndices.java` 修改（要点，保持既有代码风格）：

1. 新增字段：

```java
private final java.util.concurrent.ConcurrentMap<String, Long> versionMap =
        new java.util.concurrent.ConcurrentHashMap<>();
```

2. `insert`/`update`/`delete` 改造：把三段重复的"遍历 indexMap + 两阶段提交"抽成私有方法
   `applyAll(E extension, java.util.function.BiFunction<Index<E, ?>, E, TransactionalOperation> opFactory,
   String primaryKey, Long version, boolean deletion)`；行为变化：
   - `update` 开头加版本守卫：`var recorded = versionMap.get(primaryKey); if (version != null && recorded != null && version < recorded) { return; }`
   - 所有 `ops.forEach(TransactionalOperation::commit)` 之后：非删除且 `version != null` 时 `versionMap.put(primaryKey, version)`；删除时 `versionMap.remove(primaryKey)`（即 version 映射在索引条目提交之后更新）。
3. 新增方法：

```java
    @Override
    public void deleteByName(String primaryKey) {
        ensureNotClosed();
        var lock = Objects.requireNonNull(lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock()))
                .writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                var op = index.prepareDelete(primaryKey);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
            versionMap.remove(primaryKey);
        } catch (Exception e) {
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void updateIndices(E extension, Set<String> indexNames) {
        ensureNotClosed();
        var primaryKey = extension.getMetadata().getName();
        var lock = Objects.requireNonNull(lockCache.get(primaryKey, pk -> new ReentrantReadWriteLock()))
                .writeLock();
        var ops = new ArrayList<TransactionalOperation>();
        lock.lock();
        try {
            for (var index : indexMap.values()) {
                if (!indexNames.contains(index.getName())) {
                    continue;
                }
                var op = index.prepareUpdate(extension);
                op.prepare();
                ops.add(op);
            }
            ops.forEach(TransactionalOperation::commit);
            var version = extension.getMetadata().getVersion();
            if (version != null) {
                versionMap.put(primaryKey, version);
            }
        } catch (Exception e) {
            ops.forEach(TransactionalOperation::rollback);
            throw e;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public IndicesSnapshot dump() {
        // Capture the manifest BEFORE dumping indices: a torn dump then always satisfies
        // "manifest version <= index content", which delta recovery can only over-fetch from.
        var versions = Map.copyOf(versionMap);
        var snapshots = indexMap.values().stream().map(Index::dump).toList();
        return new IndicesSnapshot(snapshots, versions);
    }

    @Override
    public void restore(IndicesSnapshot snapshot) {
        for (var indexSnapshot : snapshot.indices()) {
            var index = indexMap.get(indexSnapshot.name());
            if (index != null) {
                index.restore(indexSnapshot);
            }
        }
        versionMap.putAll(snapshot.versions());
    }

    @Override
    public Map<String, String> currentFingerprints() {
        var result = new LinkedHashMap<String, String>();
        indexMap.values().forEach(index -> result.put(index.getName(), index.getFingerprint()));
        return result;
    }
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.*'`
Expected: 全部 PASS（含既有 `DefaultIndicesTest`、`DefaultIndexEngineTest` 等）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/Indices.java \
  application/src/main/java/run/halo/app/extension/index/DefaultIndices.java \
  application/src/test/java/run/halo/app/extension/index/DefaultIndicesSnapshotTest.java
git commit -m "feat: add version manifest, version guard and snapshot support to DefaultIndices"
```

---

### Task 5: 就绪门闩（IndicesManager + DefaultIndexEngine）

**Files:**
- Modify: `application/src/main/java/run/halo/app/extension/index/IndicesManager.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/DefaultIndicesManager.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/DefaultIndexEngine.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndicesReadinessTest.java`

**Interfaces:**
- Produces:
- `IndicesManager#awaitReady(Class<? extends Extension> type)`：门闩未就绪则阻塞至超时（30s），超时抛 `IllegalStateException`；类型未注册抛 `IllegalArgumentException`。
- `IndicesManager#markReady(Class<? extends Extension> type)`：`countDown`，幂等。
- `DefaultIndexEngine` 的 `retrieve/retrieveAll/retrieveTopN/count` 在 `indicesManager.get(type)` 之前调用 `awaitReady(type)`。
- Task 8（Initializer）调用 `markReady`。

- [ ] **Step 1: Write the failing test**

`IndicesReadinessTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import run.halo.app.extension.Extension;
import run.halo.app.extension.Metadata;

class IndicesReadinessTest {

    @Test
    void awaitReadyShouldBlockUntilMarkReady() throws InterruptedException {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        var queried = new AtomicBoolean(false);
        var threadStarted = new CountDownLatch(1);
        var thread = new Thread(() -> {
            threadStarted.countDown();
            manager.awaitReady(FakeExt.class);
            queried.set(true);
        });
        thread.start();
        assertThat(threadStarted.await(5, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(200);
        assertThat(queried).isFalse();
        manager.markReady(FakeExt.class);
        thread.join(5000);
        assertThat(queried).isTrue();
    }

    @Test
    void awaitReadyShouldThrowForUnknownType() {
        var manager = new DefaultIndicesManager();
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markReadyShouldBeIdempotent() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markReady(FakeExt.class);
        manager.markReady(FakeExt.class);
        manager.awaitReady(FakeExt.class); // returns immediately
    }

    @Test
    void removeShouldDiscardLatch() {
        var manager = new DefaultIndicesManager();
        manager.add(FakeExt.class, List.of());
        manager.markReady(FakeExt.class);
        manager.remove(FakeExt.class);
        assertThatThrownBy(() -> manager.awaitReady(FakeExt.class))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class FakeExt implements Extension {
        private final Metadata metadata = new Metadata();

        @Override
        public Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

超时路径（30s）不写真实等待测试；把超时做成 `DefaultIndicesManager` 的包级可配置字段（构造器或 setter）以便测试用 100ms 验证超时：

```java
@Test
void awaitReadyShouldTimeout() {
    var manager = new DefaultIndicesManager();
    manager.setReadyTimeout(Duration.ofMillis(100));
    manager.add(FakeExt.class, List.of());
    assertThatThrownBy(() -> manager.awaitReady(FakeExt.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not ready");
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndicesReadinessTest'`
Expected: 编译失败。

- [ ] **Step 3: Implement**

`IndicesManager.java` 增加：

```java
    /**
     * Awaits until the indices for the given type are fully built or restored, with a timeout.
     *
     * @param type the extension type
     * @throws IllegalArgumentException if no indices are registered for the type
     * @throws IllegalStateException if the indices are not ready within the timeout
     */
    void awaitReady(Class<? extends Extension> type);

    /**
     * Marks the indices for the given type as ready. Idempotent.
     *
     * @param type the extension type
     */
    void markReady(Class<? extends Extension> type);
```

`DefaultIndicesManager.java`：

```java
    private static final Duration DEFAULT_READY_TIMEOUT = Duration.ofSeconds(30);

    private final ConcurrentMap<Class<? extends Extension>, CountDownLatch> readyLatches =
            new ConcurrentHashMap<>();

    private Duration readyTimeout = DEFAULT_READY_TIMEOUT;

    /** Only for testing. */
    void setReadyTimeout(Duration readyTimeout) {
        this.readyTimeout = readyTimeout;
    }
```

`add(...)` 的 `computeIfAbsent` 之前加：

```java
readyLatches.putIfAbsent(type, new CountDownLatch(1));
```

`remove(...)` 中加 `readyLatches.remove(type);`。

新增：

```java
    @Override
    public void awaitReady(Class<? extends Extension> type) {
        var latch = readyLatches.get(type);
        if (latch == null) {
            throw new IllegalArgumentException("No indices found for type: " + type.getName());
        }
        try {
            if (!latch.await(readyTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException(
                        "Indices are not ready for type: %s after %s".formatted(type.getName(), readyTimeout));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while awaiting indices readiness for type: " + type.getName(), e);
        }
    }

    @Override
    public void markReady(Class<? extends Extension> type) {
        var latch = readyLatches.get(type);
        if (latch != null) {
            latch.countDown();
        }
    }
```

（同时补 `java.time.Duration`、`java.util.concurrent.CountDownLatch`、`java.util.concurrent.TimeUnit` import。）

`DefaultIndexEngine.java`：`retrieve`、`retrieveAll`、`retrieveTopN`、`count` 四个方法中，`var indices = indicesManager.get(type);` 之前加：

```java
indicesManager.awaitReady(type);
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.*'`
Expected: 全部 PASS。注意：既有 `DefaultIndexEngineTest` 若直接 `add` 后查询而没有 `markReady`，会在 `awaitReady` 阻塞 30s——因此实现后需检查该测试并在 `add` 后补 `manager.markReady(...)`（或在测试 setup 中统一标记）。这一步允许修改既有测试的 setup，但不允许改既有断言语义。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/ application/src/test/java/run/halo/app/extension/index/IndicesReadinessTest.java
git commit -m "feat: introduce readiness latch for indices to gate queries during build"
```

---

### Task 6: 窄投影查询（name + version）

**Files:**
- Create: `application/src/main/java/run/halo/app/extension/store/NameVersion.java`
- Modify: `application/src/main/java/run/halo/app/extension/store/ExtensionStoreRepository.java`
- Modify: `application/src/main/java/run/halo/app/extension/store/ReactiveExtensionStoreClient.java`
- Modify: `application/src/main/java/run/halo/app/extension/store/ReactiveExtensionStoreClientImpl.java`
- Modify: `application/src/main/java/run/halo/app/extension/store/ExtensionStoreClient.java`
- Modify: `application/src/main/java/run/halo/app/extension/store/ExtensionStoreClientJPAImpl.java`
- Test: `application/src/test/java/run/halo/app/extension/store/ExtensionStoreNameVersionTest.java`

**Interfaces:**
- Produces: `record NameVersion(String name, Long version)`；阻塞版 `ExtensionStoreClient#listNameVersionsByNamePrefix(String prefix) → List<NameVersion>`（Task 8 的 Initializer 只用阻塞版）。

- [ ] **Step 1: Write the failing test**

`ExtensionStoreNameVersionTest.java`（参考 `application/src/test/java/run/halo/app/extension/store/` 下既有测试的 R2DBC 测试基座；若已有 `@DataR2dbcTest` 风格的测试类，沿用其配置）：

```java
package run.halo.app.extension.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

@ExtendWith(MockitoExtension.class)
class ExtensionStoreNameVersionTest {

    @Test
    void reactiveClientShouldDelegateToRepository() {
        var repository = Mockito.mock(ExtensionStoreRepository.class);
        var entityOperations = Mockito.mock(
                org.springframework.data.r2dbc.core.R2dbcEntityOperations.class);
        Mockito.when(repository.findAllNameVersionByNameLike("/registry/posts/%"))
                .thenReturn(Flux.just(new NameVersion("/registry/posts/a", 1L)));
        var client = new ReactiveExtensionStoreClientImpl(repository, entityOperations);

        var result = client.listNameVersionsByNamePrefix("/registry/posts").collectList().block();

        assertThat(result).containsExactly(new NameVersion("/registry/posts/a", 1L));
    }

    @Test
    void blockingClientShouldReturnList() {
        var reactive = Mockito.mock(ReactiveExtensionStoreClient.class);
        Mockito.when(reactive.listNameVersionsByNamePrefix("/registry/posts"))
                .thenReturn(Flux.just(new NameVersion("/registry/posts/a", 3L)));
        var client = new ExtensionStoreClientJPAImpl(reactive);

        List<NameVersion> result = client.listNameVersionsByNamePrefix("/registry/posts");

        assertThat(result).containsExactly(new NameVersion("/registry/posts/a", 3L));
    }
}
```

注：先 `ls application/src/test/java/run/halo/app/extension/store/` 确认 `ReactiveExtensionStoreClientImpl` 构造器参数顺序与本测试一致；不一致则按实际调整。

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.store.ExtensionStoreNameVersionTest'`
Expected: 编译失败，`NameVersion` 不存在。

- [ ] **Step 3: Implement**

`NameVersion.java`：

```java
package run.halo.app.extension.store;

/**
 * Narrow projection of {@link ExtensionStore} containing only name and version, used by
 * index snapshot delta recovery to avoid loading the BLOB data column.
 *
 * @since 2.23.0
 */
public record NameVersion(String name, Long version) {}
```

`ExtensionStoreRepository.java` 增加：

```java
/**
 * Finds only name and version of all ExtensionStores matching the name like pattern.
 *
 * @param nameLike the name like pattern, e.g. {@code "/registry/posts/%"}
 * @return a flux of name-version projections
 */
@org.springframework.data.r2dbc.repository.Query(
        "SELECT name, version FROM extensions WHERE name LIKE :nameLike")
reactor.core.publisher.Flux<NameVersion> findAllNameVersionByNameLike(String nameLike);
```

（import 提到文件头，遵循 Spotless。）

`ReactiveExtensionStoreClient.java` 增加：

```java
/**
 * Lists name and version only, by name prefix. Does not load the data column.
 *
 * @param prefix the name prefix
 * @return a flux of name-version projections
 */
Flux<NameVersion> listNameVersionsByNamePrefix(String prefix);
```

`ReactiveExtensionStoreClientImpl.java` 增加：

```java
@Override
public Flux<NameVersion> listNameVersionsByNamePrefix(String prefix) {
    Assert.hasText(prefix, "Prefix must not be blank");
    prefix = Strings.CS.appendIfMissing(prefix, "/");
    return repository.findAllNameVersionByNameLike(prefix + "%");
}
```

`ExtensionStoreClient.java` 增加：

```java
/**
 * Lists name and version only, by name prefix. Does not load the data column.
 *
 * @param prefix the name prefix
 * @return list of name-version projections
 */
List<NameVersion> listNameVersionsByNamePrefix(String prefix);
```

`ExtensionStoreClientJPAImpl.java` 增加：

```java
@Override
public List<NameVersion> listNameVersionsByNamePrefix(String prefix) {
    return storeClient.listNameVersionsByNamePrefix(prefix).collectList().block(TIMEOUT);
}
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.store.*'`
Expected: PASS。再跑一次 `./gradlew :application:compileJava` 确认无其他 `ExtensionStoreClient` 实现类需要补新方法（若编译报"class must implement method"，按同样的委托模式补齐）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/store/ application/src/test/java/run/halo/app/extension/store/ExtensionStoreNameVersionTest.java
git commit -m "feat: add narrow name-version projection query for extension stores"
```

---

### Task 7: 快照编解码 IndexSnapshotCodec

**Files:**
- Create: `application/src/main/java/run/halo/app/extension/index/IndexSnapshotCodec.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndexSnapshotCodecTest.java`

**Interfaces:**
- Consumes: Task 3 的 `IndicesSnapshot`/`IndexSnapshot`。
- Produces: `IndexSnapshotCodec.write(IndicesSnapshot, OutputStream)`、`IndexSnapshotCodec.read(InputStream) → IndicesSnapshot`；损坏/格式不符时抛 `IndexSnapshotCorruptedException`（新建运行时异常，放同文件或同包）。Task 8 的 Manager 依赖。

- [ ] **Step 1: Write the failing test**

`IndexSnapshotCodecTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IndexSnapshotCodecTest {

    @Test
    void shouldRoundTrip() throws Exception {
        var snapshot = new IndicesSnapshot(
                List.of(
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
                                List.of())),
                Map.of("p1", 1L, "p2", 2L, "p3", 7L, "p4", 9L));

        var out = new ByteArrayOutputStream();
        IndexSnapshotCodec.write(snapshot, out);
        var restored = IndexSnapshotCodec.read(new ByteArrayInputStream(out.toByteArray()));

        assertThat(restored).isEqualTo(snapshot);
    }

    @Test
    void shouldRejectBadMagic() {
        var garbage = new byte[] {0x1f, (byte) 0x8b, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(garbage)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }

    @Test
    void shouldRejectTruncatedStream() throws Exception {
        var snapshot = new IndicesSnapshot(List.of(), Map.of("a", 1L));
        var out = new ByteArrayOutputStream();
        IndexSnapshotCodec.write(snapshot, out);
        var truncated = new byte[out.size() / 2];
        System.arraycopy(out.toByteArray(), 0, truncated, 0, truncated.length);
        assertThatThrownBy(() -> IndexSnapshotCodec.read(new ByteArrayInputStream(truncated)))
                .isInstanceOf(IndexSnapshotCorruptedException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexSnapshotCodecTest'`
Expected: 编译失败。

- [ ] **Step 3: Implement**

`IndexSnapshotCodec.java`：

```java
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
 * @since 2.23.0
 */
final class IndexSnapshotCodec {

    private static final int MAGIC = 0x48414953; // 'HAIS'

    private static final int FORMAT_VERSION = 1;

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
            var indexCount = data.readInt();
            var indices = new ArrayList<IndexSnapshot>(indexCount);
            for (var i = 0; i < indexCount; i++) {
                var name = data.readUTF();
                var fingerprint = data.readUTF();
                var keyType = data.readUTF();
                var nullKeyCount = data.readInt();
                var nullKeys = new ArrayList<String>(nullKeyCount);
                for (var j = 0; j < nullKeyCount; j++) {
                    nullKeys.add(data.readUTF());
                }
                var entryCount = data.readInt();
                var entries = new ArrayList<IndexSnapshot.Entry>(entryCount);
                for (var j = 0; j < entryCount; j++) {
                    var partCount = data.readInt();
                    var keyParts = new ArrayList<String>(partCount);
                    for (var k = 0; k < partCount; k++) {
                        keyParts.add(data.readUTF());
                    }
                    var pkCount = data.readInt();
                    var primaryKeys = new ArrayList<String>(pkCount);
                    for (var k = 0; k < pkCount; k++) {
                        primaryKeys.add(data.readUTF());
                    }
                    entries.add(new IndexSnapshot.Entry(List.copyOf(keyParts), List.copyOf(primaryKeys)));
                }
                indices.add(new IndexSnapshot(
                        name, fingerprint, keyType, List.copyOf(entries), List.copyOf(nullKeys)));
            }
            var versionCount = data.readInt();
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
}

同文件内增加异常类：

```java
/** Thrown when an index snapshot file is corrupted or has an unsupported format. */
class IndexSnapshotCorruptedException extends RuntimeException {

    IndexSnapshotCorruptedException(String message) {
        super(message);
    }

    IndexSnapshotCorruptedException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexSnapshotCodecTest'`
Expected: PASS（3 个用例）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/IndexSnapshotCodec.java \
  application/src/test/java/run/halo/app/extension/index/IndexSnapshotCodecTest.java
git commit -m "feat: add binary codec for index snapshots"
```

---

### Task 8: IndexSnapshotManager（文件存取）

**Files:**
- Create: `application/src/main/java/run/halo/app/extension/index/IndexSnapshotManager.java`
- Test: `application/src/test/java/run/halo/app/extension/index/IndexSnapshotManagerTest.java`

**Interfaces:**
- Consumes: Task 7 的 `IndexSnapshotCodec`；`run.halo.app.infra.properties.HaloProperties#getWorkDir()`（实现时先确认 getter 名与类型 `Path`）。
- Produces:
- `IndexSnapshotManager#save(Class<? extends Extension> type, IndicesSnapshot snapshot)`：临时文件 + `ATOMIC_MOVE` rename；IO 失败只记 error 日志，不抛出。
- `IndexSnapshotManager#load(Class<? extends Extension> type) → Optional<IndicesSnapshot>`：文件不存在→empty；损坏→记 warn、删除坏文件、返回 empty。
- 快照路径：`<workDir>/indices/<type.getName()>.snapshot.gz`。Task 9/10 依赖。

- [ ] **Step 1: Write the failing test**

`IndexSnapshotManagerTest.java`：

```java
package run.halo.app.extension.index;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class IndexSnapshotManagerTest {

    @TempDir
    Path workDir;

    @TempDir
    Path otherDir;

    private IndexSnapshotManager newManager() {
        return new IndexSnapshotManager(workDir);
    }

    @Test
    void saveAndLoadShouldRoundTrip() {
        var manager = newManager();
        var snapshot = new IndicesSnapshot(
                List.of(new IndexSnapshot(
                        "spec.slug", "fp", "java.lang.String",
                        List.of(new IndexSnapshot.Entry(List.of("hello"), List.of("p1"))),
                        List.of())),
                Map.of("p1", 3L));
        manager.save(FakeType.class, snapshot);

        var loaded = manager.load(FakeType.class);

        assertThat(loaded).contains(snapshot);
        // other type has no snapshot
        assertThat(manager.load(OtherType.class)).isEmpty();
    }

    @Test
    void loadShouldReturnEmptyForCorruptedFileAndDeleteIt() throws Exception {
        var manager = newManager();
        var file = workDir.resolve("indices").resolve(FakeType.class.getName() + ".snapshot.gz");
        Files.createDirectories(file.getParent());
        Files.write(file, new byte[] {1, 2, 3, 4, 5});

        assertThat(manager.load(FakeType.class)).isEmpty();
        assertThat(file).doesNotExist();
    }

    @Test
    void saveFailureShouldNotThrow() {
        // 指向一个不可创建的路径（文件当目录用）制造失败
        var blocker = workDir.resolve("blocker");
        try {
            Files.writeString(blocker, "x");
            var manager = new IndexSnapshotManager(blocker.resolve("indices"));
            manager.save(FakeType.class, new IndicesSnapshot(List.of(), Map.of()));
            // no exception expected
        } catch (Exception e) {
            throw new AssertionError("save should swallow IO errors", e);
        }
    }

    static class FakeType implements run.halo.app.extension.Extension {
        private final run.halo.app.extension.Metadata metadata = new run.halo.app.extension.Metadata();

        @Override
        public run.halo.app.extension.Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(run.halo.app.extension.Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }

    static class OtherType extends FakeType {}
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexSnapshotManagerTest'`
Expected: 编译失败。

- [ ] **Step 3: Implement**

`IndexSnapshotManager.java`：

```java
package run.halo.app.extension.index;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Extension;
import run.halo.app.infra.properties.HaloProperties;

/**
 * Persists and loads index snapshots under {@code <work-dir>/indices/}. All failures are
 * logged and degraded to a full index rebuild; nothing here may break startup or shutdown.
 *
 * @since 2.23.0
 */
@Slf4j
@Component
public class IndexSnapshotManager {

    private final Path snapshotDir;

    @org.springframework.beans.factory.annotation.Autowired
    public IndexSnapshotManager(HaloProperties haloProperties) {
        this(haloProperties.getWorkDir().resolve("indices"));
    }

    /** Also usable in tests with an arbitrary directory. */
    public IndexSnapshotManager(Path snapshotDir) {
        this.snapshotDir = snapshotDir;
    }

    public void save(Class<? extends Extension> type, IndicesSnapshot snapshot) {
        var file = snapshotFile(type);
        var tempFile = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.createDirectories(snapshotDir);
            try (OutputStream out = Files.newOutputStream(tempFile)) {
                IndexSnapshotCodec.write(snapshot, out);
            }
            Files.move(tempFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            log.error("Failed to save index snapshot for type {}", type.getName(), e);
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
        }
    }

    public Optional<IndicesSnapshot> load(Class<? extends Extension> type) {
        var file = snapshotFile(type);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(file)) {
            return Optional.of(IndexSnapshotCodec.read(in));
        } catch (IndexSnapshotCorruptedException | IOException e) {
            log.warn("Index snapshot for type {} is corrupted ({}), falling back to full build",
                    type.getName(), e.getMessage());
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // ignore cleanup failure
            }
            return Optional.empty();
        }
    }

    private Path snapshotFile(Class<? extends Extension> type) {
        return snapshotDir.resolve(type.getName() + ".snapshot.gz");
    }
}
```

注：`ATOMIC_MOVE` 在不支持的文件系统上会抛 `AtomicMoveNotSupportedException`（是 `IOException` 子类），被 catch 后降级为"不存快照"，可接受；如需更稳妥可先 try atomic、失败后降级普通 move，实现时二选一并在代码注释说明。

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.index.IndexSnapshotManagerTest'`
Expected: PASS（3 个用例）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/index/IndexSnapshotManager.java \
  application/src/test/java/run/halo/app/extension/index/IndexSnapshotManagerTest.java
git commit -m "feat: add index snapshot manager for persisting indices to work-dir"
```

---

### Task 9: DefaultIndicesInitializer — 三级恢复 + IndexEngine.deleteByName

**Files:**
- Modify: `application/src/main/java/run/halo/app/extension/indexer/DefaultIndicesInitializer.java`
- Modify: `application/src/main/java/run/halo/app/extension/index/IndexEngine.java`（新增 `deleteByName`）
- Modify: `application/src/main/java/run/halo/app/extension/index/DefaultIndexEngine.java`
- Test: `application/src/test/java/run/halo/app/extension/indexer/DefaultIndicesInitializerTest.java`

**Interfaces:**
- Consumes: Task 4 的 `Indices#restore/updateIndices/deleteByName/currentFingerprints`，Task 5 的 `markReady`，Task 6 的 `ExtensionStoreClient#listNameVersionsByNamePrefix`，Task 8 的 `IndexSnapshotManager#load`。
- Produces: `IndexEngine#deleteByName(Class<E> type, String primaryKey)`；Initializer 新构造器签名 `DefaultIndicesInitializer(IndexEngine, ExtensionStoreClient, ExtensionConverter, IndexSnapshotManager)`。

- [ ] **Step 1: Write the failing test**

`DefaultIndicesInitializerTest.java`（包级可见类，同包测试；`FakePost` 结构同 Task 4，需加 `@GVK` 注解以便 `Scheme.buildFromType`）：

```java
package run.halo.app.extension.indexer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.convert.support.DefaultConversionService;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.index.DefaultIndexEngine;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.IndicesSnapshot;
import run.halo.app.extension.index.IndexSnapshot;
import run.halo.app.extension.index.IndexSpecs;
import run.halo.app.extension.indexer.DefaultIndicesInitializer;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;
import run.halo.app.extension.store.NameVersion;

class DefaultIndicesInitializerTest {

    @TempDir
    Path workDir;

    DefaultIndexEngine indexEngine;
    ExtensionStoreClient storeClient;
    ExtensionConverter converter;
    IndexSnapshotManager snapshotManager;
    DefaultIndicesInitializer initializer;
    Scheme scheme;

    @BeforeEach
    void setUp() {
        indexEngine = new DefaultIndexEngine(new DefaultConversionService());
        storeClient = mock(ExtensionStoreClient.class);
        converter = mock(ExtensionConverter.class);
        snapshotManager = new IndexSnapshotManager(workDir.resolve("indices"));
        initializer = new DefaultIndicesInitializer(indexEngine, storeClient, converter, snapshotManager);
        scheme = Scheme.buildFromType(FakePost.class);
        lenient().when(converter.convertFrom(any(), any(ExtensionStore.class)))
                .thenAnswer(inv -> {
                    ExtensionStore es = inv.getArgument(1);
                    var name = es.getName().substring(es.getName().lastIndexOf('/') + 1);
                    var post = new FakePost(name, "slug-" + name);
                    post.getMetadata().setVersion(es.getVersion());
                    return post;
                });
    }

    private void registerType() {
        indexEngine.getIndicesManager().add(FakePost.class, List.of(
                IndexSpecs.<FakePost, String>single("spec.slug", String.class)
                        .indexFunc(p -> p.slug)
                        .nullable(false)
                        .build()));
    }

    private ExtensionStore storeOf(String name, long version) {
        return new ExtensionStore("/registry/fakeposts/" + name, new byte[] {1}, version);
    }

    @Test
    void shouldFullBuildWhenNoSnapshot() {
        registerType();
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L), storeOf("b", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null))
                .containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldRestoreFromSnapshotAndApplyDelta() {
        registerType();
        // 快照里有 a(v1)、gone(v1)；DB 里是 a(v1)、b(v2) → a 不动、b 拉取、gone 删除
        var indices = indexEngine.getIndicesManager().get(FakePost.class);
        indices.updateIndices(fakePost("a", 1L), Set.of("spec.slug"));
        indices.updateIndices(fakePost("gone", 1L), Set.of("spec.slug"));
        snapshotManager.save(FakePost.class, indices.dump());
        indexEngine.getIndicesManager().remove(FakePost.class);
        registerType();

        when(storeClient.listNameVersionsByNamePrefix("/registry/fakeposts"))
                .thenReturn(List.of(
                        new NameVersion("/registry/fakeposts/a", 1L),
                        new NameVersion("/registry/fakeposts/b", 2L)));
        when(storeClient.listByNames(List.of("/registry/fakeposts/b")))
                .thenReturn(List.of(storeOf("b", 2L)));

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null))
                .containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void shouldFallBackToFullBuildOnCorruptedSnapshot() {
        registerType();
        snapshotManager.save(FakePost.class, new IndicesSnapshot(List.of(), Map.of("x", 1L)));
        // 写入一个 keyType 对不上的快照条目制造 restore 异常
        when(storeClient.listBy(anyString(), any(), anyInt()))
                .thenReturn(List.of(storeOf("a", 1L)))
                .thenReturn(List.of());

        initializer.doInitialize(scheme);

        assertThat(indexEngine.retrieveAll(FakePost.class, null, null)).containsExactly("a");
    }

    private static FakePost fakePost(String name, long version) {
        var post = new FakePost(name, "slug-" + name);
        post.getMetadata().setVersion(version);
        return post;
    }

    @GroupVersionKind(group = "test.halo.run", version = "v1alpha1", kind = "FakePost", plural = "fakeposts", singular = "fakepost")
    static class FakePost implements run.halo.app.extension.Extension {
        private final run.halo.app.extension.Metadata metadata = new run.halo.app.extension.Metadata();
        String slug;

        FakePost(String name, String slug) {
            metadata.setName(name);
            this.slug = slug;
        }

        @Override
        public run.halo.app.extension.Metadata getMetadata() {
            return metadata;
        }

        @Override
        public void setMetadata(run.halo.app.extension.Metadata metadata) {
            throw new UnsupportedOperationException();
        }
    }
}
```

注：`@GVK` 注解的实际类名/包以 `Scheme.buildFromType` 所依赖的注解为准（先查 `api/src/main/java/run/halo/app/extension/Scheme.java` 与 `GVK.java`）；`DefaultIndexEngine` 构造器是包级私有的，本测试在 `indexer` 包无法直接 new——实现时需二选一：(a) 把测试放进 `index` 包测 `DefaultIndexEngine` 组装、Initializer 逻辑用 mock 的 `IndexEngine`；(b) 给 `DefaultIndexEngine` 加包级工厂方法。**推荐 (a)**：Initializer 的测试 mock `IndexEngine` + 真实 `DefaultIndicesManager` 经 `IndexEngine#getIndicesManager` 暴露（`IndexEngine` 接口已有 `getIndicesManager()`）。按此调整上面的测试代码：mock `IndexEngine`，`when(indexEngine.getIndicesManager()).thenReturn(realManager)`，`indexEngine.insert/update/deleteByName` 用 thenAnswer 委托给真实 manager 操作，或直接用 `Indices` 断言替代 `retrieveAll` 断言。

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.indexer.DefaultIndicesInitializerTest'`
Expected: 编译失败（构造器签名、`deleteByName`、`listNameVersionsByNamePrefix` 不存在）。

- [ ] **Step 3: Implement**

`IndexEngine.java` 增加：

```java
/**
 * Deletes all index entries for the given primary key of the given type.
 *
 * @param type the extension type
 * @param primaryKey the primary key
 * @param <E> the extension type
 */
<E extends Extension> void deleteByName(Class<E> type, String primaryKey);
```

`DefaultIndexEngine.java` 增加：

```java
@Override
public <E extends Extension> void deleteByName(Class<E> type, String primaryKey) {
    indicesManager.get(type).deleteByName(primaryKey);
}
```

`DefaultIndicesInitializer.java` 全量替换为（构造器加 `IndexSnapshotManager`）：

```java
package run.halo.app.extension.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionConverter;
import run.halo.app.extension.ExtensionStoreUtil;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.event.SchemeAddedEvent;
import run.halo.app.extension.index.IndexEngine;
import run.halo.app.extension.index.IndexSnapshotManager;
import run.halo.app.extension.index.Indices;
import run.halo.app.extension.index.IndicesInitializer;
import run.halo.app.extension.index.IndicesSnapshot;
import run.halo.app.extension.store.ExtensionStore;
import run.halo.app.extension.store.ExtensionStoreClient;

@Component
@Slf4j
class DefaultIndicesInitializer implements IndicesInitializer {

    private static final int BATCH_SIZE = 100;

    private final IndexEngine indexEngine;

    private final ExtensionStoreClient client;

    private final ExtensionConverter extensionConverter;

    private final IndexSnapshotManager snapshotManager;

    DefaultIndicesInitializer(
            IndexEngine indexEngine,
            ExtensionStoreClient client,
            ExtensionConverter extensionConverter,
            IndexSnapshotManager snapshotManager) {
        this.indexEngine = indexEngine;
        this.client = client;
        this.extensionConverter = extensionConverter;
        this.snapshotManager = snapshotManager;
    }

    @EventListener
    @Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
    void onSchemeAddedEvent(SchemeAddedEvent event) {
        this.initialize(event.getScheme());
    }

    @Override
    public void initialize(Scheme scheme) {
        doInitialize(scheme);
    }

    public <E extends Extension> void doInitialize(Scheme scheme) {
        var type = (Class<E>) scheme.type();
        var prefix = ExtensionStoreUtil.buildStoreNamePrefix(scheme);
        try {
            var loaded = snapshotManager.load(type);
            if (loaded.isPresent()) {
                try {
                    restoreFromSnapshot(type, prefix, loaded.get());
                    return;
                } catch (Exception e) {
                    log.warn(
                            "Failed to restore indices for type {} from snapshot, falling back to full build",
                            type.getName(),
                            e);
                }
            }
            fullBuild(type, prefix);
        } finally {
            indexEngine.getIndicesManager().markReady(type);
        }
    }

    private <E extends Extension> void restoreFromSnapshot(
            Class<E> type, String prefix, IndicesSnapshot snapshot) {
        var watch = new StopWatch("Restore indices for " + type.getName());
        watch.start("restore snapshot");
        var indices = indexEngine.getIndicesManager().get(type);
        var currentFingerprints = indices.currentFingerprints();
        var matched = snapshot.indices().stream()
                .filter(s -> Objects.equals(s.fingerprint(), currentFingerprints.get(s.name())))
                .toList();
        var matchedNames = matched.stream().map(IndexSnapshot::name).collect(Collectors.toSet());
        var dirtyIndexNames = currentFingerprints.keySet().stream()
                .filter(name -> !matchedNames.contains(name))
                .collect(Collectors.toSet());
        indices.restore(new IndicesSnapshot(matched, snapshot.versions()));
        watch.stop();
        if (dirtyIndexNames.isEmpty()) {
            deltaRecover(type, prefix, snapshot.versions(), watch);
        } else {
            log.info("Indices {} of type {} need rebuild, scanning full table",
                    dirtyIndexNames, type.getName());
            mergedScanRecover(type, prefix, snapshot.versions(), dirtyIndexNames, watch);
        }
        log.info("Restored indices for type {}, summary: {}", type.getName(),
                watch.prettyPrint(TimeUnit.MILLISECONDS));
    }

    /** Delta recovery via the narrow name/version query; no full-table data scan. */
    private <E extends Extension> void deltaRecover(
            Class<E> type, String prefix, Map<String, Long> manifest, StopWatch watch) {
        watch.start("delta recover");
        var dbRows = client.listNameVersionsByNamePrefix(prefix);
        var manifestByStoreName = new HashMap<String, Long>();
        manifest.forEach((name, version) -> manifestByStoreName.put(prefix + "/" + name, version));
        var dbStoreNames = new HashSet<String>();
        var staleStoreNames = new ArrayList<String>();
        for (var row : dbRows) {
            dbStoreNames.add(row.name());
            var recorded = manifestByStoreName.get(row.name());
            if (recorded == null || !recorded.equals(row.version())) {
                staleStoreNames.add(row.name());
            }
        }
        for (var from = 0; from < staleStoreNames.size(); from += BATCH_SIZE) {
            var batch = staleStoreNames.subList(from, Math.min(from + BATCH_SIZE, staleStoreNames.size()));
            var stores = client.listByNames(batch);
            indexEngine.insert(stores.stream()
                    .map(es -> this.extensionConverter.convertFrom(type, es))::iterator);
        }
        manifestByStoreName.keySet().stream()
                .filter(storeName -> !dbStoreNames.contains(storeName))
                .map(storeName -> storeName.substring(prefix.length() + 1))
                .forEach(name -> indexEngine.deleteByName(type, name));
        watch.stop();
    }

    /** Merged full-table scan: rebuild dirty indices and apply the delta in one pass. */
    private <E extends Extension> void mergedScanRecover(
            Class<E> type,
            String prefix,
            Map<String, Long> manifest,
            Set<String> dirtyIndexNames,
            StopWatch watch) {
        watch.start("merged scan recover");
        var indices = indexEngine.getIndicesManager().get(type);
        var seenNames = new HashSet<String>();
        List<ExtensionStore> stores;
        String nameCursor = null;
        do {
            stores = client.listBy(prefix, nameCursor, BATCH_SIZE);
            for (var store : stores) {
                var extension = this.extensionConverter.<E>convertFrom(type, store);
                var name = extension.getMetadata().getName();
                seenNames.add(name);
                var recorded = manifest.get(name);
                if (recorded != null && recorded.equals(store.getVersion())) {
                    // unchanged row: only fill the dirty indices
                    indices.updateIndices(extension, dirtyIndexNames);
                } else {
                    // new or changed row: upsert all indices
                    indexEngine.insert(List.of(extension));
                }
            }
            if (!stores.isEmpty()) {
                nameCursor = stores.getLast().getName();
            }
        } while (!stores.isEmpty());
        manifest.keySet().stream()
                .filter(name -> !seenNames.contains(name))
                .forEach(name -> indexEngine.deleteByName(type, name));
        watch.stop();
    }

    private <E extends Extension> void fullBuild(Class<E> type, String prefix) {
        List<ExtensionStore> extensionStores;
        String nameCursor = null;
        log.info("Start to initialize indices for type: {}, prefix: {}", type.getName(), prefix);
        var watch = new StopWatch("Initialize indices for " + type.getName());
        var indexedCount = 0L;
        do {
            watch.start("Indexing from " + (nameCursor == null ? "@start" : nameCursor));
            extensionStores = client.listBy(prefix, nameCursor, BATCH_SIZE);
            indexEngine.insert(
                    extensionStores.stream().map(es -> this.extensionConverter.convertFrom(type, es))::iterator);
            if (!extensionStores.isEmpty()) {
                nameCursor = extensionStores.getLast().getName();
            }
            indexedCount += extensionStores.size();
            watch.stop();
        } while (!extensionStores.isEmpty());
        log.info(
                "Total indexed count: {}, initialization summary: {}",
                indexedCount,
                watch.prettyPrint(TimeUnit.MILLISECONDS));
    }
}
```

注意点：
- **事件监听顺序（防死锁，必须做）**：`onSchemeAddedEvent` 上的
`@Order(Ordered.HIGHEST_PRECEDENCE)` 是硬性要求。Spring 的 `@EventListener` 在发布线程上
同步执行；`GcSynchronizer` 也监听 `SchemeAddedEvent` 且会通过 client 查询索引（走
`awaitReady` 阻塞）。若 GC 监听器先于 Initializer 执行，它会阻塞在等待门闩上，而
Initializer 永远得不到执行——死锁直到 30s 超时。Initializer 必须先于一切查询方执行。
实现后 grep 确认没有其他监听 `SchemeAddedEvent` 且会查询索引的组件需要同样约束。
- 快照不可用时 `fullBuild` 与现状一致；快照恢复失败回退 `fullBuild` 是安全的（insert 为 upsert 语义）。
- `IndexSnapshot` 需在文件头部 import（上面 import 列表中已包含 `IndicesSnapshot`，
需同时加 `run.halo.app.extension.index.IndexSnapshot`）。若编译报缺 import 按 IDE 提示补。
- 恢复回退路径中 `fullBuild` 前索引可能已被部分 restore，upsert 语义保证幂等。

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.indexer.*' --tests 'run.halo.app.extension.index.*'`
Expected: PASS。另跑 `./gradlew :application:compileJava` 确认 `DefaultIndicesInitializer` 的其他构造调用点（如有 Spring 自动装配则无需改）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/indexer/ \
  application/src/main/java/run/halo/app/extension/index/IndexEngine.java \
  application/src/main/java/run/halo/app/extension/index/DefaultIndexEngine.java
git commit -m "feat: restore indices from snapshot with delta recovery on scheme registration"
```

---

### Task 10: DefaultSchemeManager.unregister 保存快照

**Files:**
- Modify: `application/src/main/java/run/halo/app/extension/DefaultSchemeManager.java`
- Test: `application/src/test/java/run/halo/app/extension/DefaultSchemeManagerTest.java`（若已存在则追加用例）

**Interfaces:**
- Consumes: Task 4 的 `Indices#dump()`，Task 8 的 `IndexSnapshotManager#save`。
- Produces: `DefaultSchemeManager` 构造器变为 `(IndexEngine, ApplicationEventPublisher, IndexSnapshotManager)`；unregister 时在 remove 之前保存快照（异常只记日志）。

- [ ] **Step 1: Write the failing test**

先 `ls application/src/test/java/run/halo/app/extension/ | grep -i scheme` 确认是否已有 `DefaultSchemeManagerTest`，有则在其基础上追加（构造器 mock 相应增加 `IndexSnapshotManager`）。新增用例：

```java
    @Test
    void shouldSaveSnapshotOnUnregister() {
        var indexEngine = new DefaultIndexEngine(new DefaultConversionService()); // 见下注释
        var eventPublisher = mock(ApplicationEventPublisher.class);
        var snapshotManager = mock(IndexSnapshotManager.class);
        var schemeManager = new DefaultSchemeManager(indexEngine, eventPublisher, snapshotManager);
        schemeManager.register(FakeExtension.class, specs -> specs.add(
                IndexSpecs.<FakeExtension, String>single("spec.slug", String.class)
                        .indexFunc(e -> "x")
                        .build()));
        var scheme = schemeManager.get(FakeExtension.class);

        schemeManager.unregister(scheme);

        verify(snapshotManager).save(eq(FakeExtension.class), any(IndicesSnapshot.class));
    }

    @Test
    void unregisterShouldSucceedEvenIfSnapshotSaveFails() {
        // snapshotManager.save 抛 RuntimeException 时 unregister 仍完成 remove 与事件发布
        ...
        doThrow(new RuntimeException("disk full")).when(snapshotManager).save(any(), any());
        schemeManager.unregister(scheme);
        verify(eventPublisher).publishEvent(any(SchemeRemovedEvent.class));
        assertThatThrownBy(() -> schemeManager.get(FakeExtension.class))
                .isInstanceOf(SchemeNotFoundException.class);
    }
```

注：`DefaultIndexEngine` 构造器包级可见，`extension` 包测试无法直接 new——用 mock：`var indexEngine = mock(IndexEngine.class); var manager = new DefaultIndicesManager();` 同样受包限制，改为 `when(indexEngine.getIndicesManager()).thenReturn(...)` 需要一个真实 `IndicesManager`。最简方案：mock `IndicesManager` 与 `Indices`，`when(indices.dump()).thenReturn(new IndicesSnapshot(List.of(), Map.of()))`。按此调整。

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.DefaultSchemeManagerTest'`
Expected: 编译失败（构造器参数不匹配）。

- [ ] **Step 3: Implement**

`DefaultSchemeManager.java`：

```java
    private final IndexSnapshotManager snapshotManager;

    public DefaultSchemeManager(
            IndexEngine indexEngine,
            ApplicationEventPublisher eventPublisher,
            IndexSnapshotManager snapshotManager) {
        this.indexEngine = indexEngine;
        this.eventPublisher = eventPublisher;
        this.snapshotManager = snapshotManager;
        schemes = new CopyOnWriteArrayList<>();
    }
```

`unregister` 改为：

```java
    @Override
    public void unregister(Scheme scheme) {
        if (schemes.contains(scheme)) {
            saveIndexSnapshot(scheme);
            indexEngine.getIndicesManager().remove(scheme.type());
            schemes.remove(scheme);
            eventPublisher.publishEvent(new SchemeRemovedEvent(this, scheme));
        }
    }

    private void saveIndexSnapshot(Scheme scheme) {
        try {
            var indices = indexEngine.getIndicesManager().get(scheme.type());
            snapshotManager.save(scheme.type(), indices.dump());
        } catch (Exception e) {
            log.error("Failed to save index snapshot for type {}", scheme.type().getName(), e);
        }
    }
```

（类上加 `@Slf4j`，import `run.halo.app.extension.index.IndexSnapshotManager` 与 `lombok.extern.slf4j.Slf4j`。）

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.*'`
Expected: PASS（含既有 SchemeManager 相关测试；构造器变更导致的编译错误全部修完）。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/DefaultSchemeManager.java \
  application/src/test/java/run/halo/app/extension/
git commit -m "feat: save index snapshot on scheme unregistration"
```

---

### Task 11: ReactiveExtensionClientImpl — afterCommit 对齐

**Files:**
- Modify: `application/src/main/java/run/halo/app/extension/ReactiveExtensionClientImpl.java`
- Test: `application/src/test/java/run/halo/app/extension/ReactiveExtensionClientImplTest.java`（既有文件，追加用例）

**Interfaces:**
- Consumes: 无新依赖。
- Produces: 私有方法 `Mono<Void> registerIndexOperationAfterCommit(Runnable)`；`doCreate`/`doUpdate` 改为通过它在事务 afterCommit 时应用索引变更；无事务上下文时（如测试的直通 operator）降级为立即执行，保持既有行为。

- [ ] **Step 1: Write the failing test**

在既有 `ReactiveExtensionClientImplTest` 中追加（先读该文件确认现有 mock 结构；下面用例假设与现有一致的可注入 `transactionalOperator`/`scheduler`）：

```java
    @Test
    void indexShouldBeUpdatedImmediatelyWhenNoTransactionContext() {
        // 直通 operator（无事务上下文）：走 NoTransactionException 降级路径，索引立即更新
        // 断言 create 返回后 indexEngine.insert 已被调用
    }

    @Test
    void indexOperationShouldBeDeferredToAfterCommit() {
        // 用 mock 的 TransactionSynchronizationManager 静态方法不可行时，
        // 改为验证 registerIndexOperationAfterCommit 在有事务时注册 synchronization：
        // 通过 StepVerifier + TransactionalOperator.create(fakeTxManager) 验证
        // afterCommit 之前 indexEngine 未被调用、之后被调用。
    }
```

注：响应式事务同步的单测成本较高，允许第二个用例降级为**集成验证注释 + 手工验证清单**（见 Task 12）；第一个用例（无事务降级）必须真实可跑。

- [ ] **Step 2: Run existing tests to establish baseline**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.ReactiveExtensionClientImplTest'`
Expected: 改动前全部 PASS（记录基线）。

- [ ] **Step 3: Implement**

`ReactiveExtensionClientImpl.java` 增加私有方法：

```java
/**
 * Registers an index operation to be applied after the current transaction commits. If
 * there is no active transaction (e.g. in tests with a pass-through operator), the
 * operation is applied immediately, preserving the previous behavior.
 */
private Mono<Void> registerIndexOperationAfterCommit(Runnable indexOperation) {
    return TransactionSynchronizationManager.forCurrentTransaction()
            .doOnNext(tsm -> tsm.registerSynchronization(new TransactionSynchronization() {
                @Override
                public Mono<Void> afterCommit() {
                    return Mono.fromRunnable(indexOperation).subscribeOn(scheduler).then();
                }
            }))
            .then()
            .onErrorResume(
                    NoTransactionException.class,
                    e -> Mono.fromRunnable(indexOperation).subscribeOn(scheduler).then());
}
```

`doCreate` 的返回链改为：

```java
return client.create(name, data)
        .map(created -> converter.convertFrom(type, created))
        .flatMap(extension -> registerIndexOperationAfterCommit(
                        () -> this.indexEngine.insert(List.of(convertToRealExtension(extension))))
                .thenReturn(extension))
        .as(transactionalOperator::transactional);
```

`doUpdate` 同理（`indexEngine.update`）。新增 import：

```java
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;
```

- [ ] **Step 4: Run tests**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.*'`
Expected: 既有测试全部 PASS（降级路径保证兼容），新用例 PASS。

- [ ] **Step 5: Commit**

```bash
./gradlew spotlessApply
git add application/src/main/java/run/halo/app/extension/ReactiveExtensionClientImpl.java \
  application/src/test/java/run/halo/app/extension/ReactiveExtensionClientImplTest.java
git commit -m "fix: apply index mutations after database transaction commits"
```

---

### Task 12: 端到端集成测试与收尾验证

**Files:**
- Test: `application/src/test/java/run/halo/app/extension/indexer/IndexSnapshotRecoveryIntegrationTest.java`

**Interfaces:**
- Consumes: Task 1-11 全部。

注：`DefaultIndexEngine` 与 `DefaultIndicesInitializer` 目前都是包级可见类，集成测试（在
`indexer` 包）无法直接实例化 `DefaultIndexEngine`。本任务允许把 `DefaultIndexEngine` 的
类声明改为 `public class DefaultIndexEngine`（构造器本来就是 public，仅放开类可见性，
属于测试需要的最小改动），并在 Task 12 的提交中一并包含。

- [ ] **Step 0: Widen visibility**

把 `application/src/main/java/run/halo/app/extension/index/DefaultIndexEngine.java` 的
`class DefaultIndexEngine` 改为 `public class DefaultIndexEngine`，运行
`./gradlew :application:compileJava` 确认通过。

- [ ] **Step 1: Write the integration test**

场景（单测级集成，真实组件 + 内存假 `ExtensionStoreClient`，不需要 Spring 容器）：

```java
// 1. 真实 DefaultIndexEngine + DefaultIndicesManager（经 IndexEngine#getIndicesManager）、
//    真实 IndexSnapshotManager(@TempDir)、mock ExtensionConverter、内存 ExtensionStoreClient
// 2. register 类型 → doInitialize（全量构建）→ unregister 前 dump+save（模拟 Task 10 的调用序列）
// 3. 新建一套 engine/manager（模拟重启）→ doInitialize → 断言走了快照路径：
//    storeClient.listBy 零调用（无全表扫描）、listNameVersionsByNamePrefix 一次
// 4. 修改一行 version、删除一行、新增一行 → 再次"重启" → 断言差量恢复后查询结果正确
// 5. spec 加一个新索引列（register 时多一个 spec）→ 断言走了 mergedScanRecover：
//    listBy 被调用（全表扫描）、结果正确、新索引可查询
```

内存 `ExtensionStoreClient` 参考实现（放测试文件内）：

```java
static class InMemoryExtensionStoreClient implements ExtensionStoreClient {
        private final java.util.NavigableMap<String, ExtensionStore> stores = new java.util.TreeMap<>();

        @Override
        public List<ExtensionStore> listBy(String prefix, String nameCursor, int limit) {
            var from = nameCursor == null ? prefix + "/" : nameCursor;
            return stores.tailMap(from, false).values().stream()
                    .filter(es -> es.getName().startsWith(prefix + "/"))
                    .limit(limit)
                    .toList();
        }

        @Override
        public List<ExtensionStore> listByNames(List<String> names) {
            return names.stream().map(stores::get).filter(java.util.Objects::nonNull).toList();
        }

        @Override
        public List<NameVersion> listNameVersionsByNamePrefix(String prefix) {
            return stores.values().stream()
                    .filter(es -> es.getName().startsWith(prefix + "/"))
                    .map(es -> new NameVersion(es.getName(), es.getVersion()))
                    .toList();
        }

        void put(ExtensionStore store) {
            stores.put(store.getName(), store);
        }

        void remove(String name) {
            stores.remove(name);
        }

        // 其余接口方法 throw new UnsupportedOperationException()
    }
```

- [ ] **Step 2: Run the integration test**

Run: `./gradlew :application:test --tests 'run.halo.app.extension.indexer.IndexSnapshotRecoveryIntegrationTest'`
Expected: PASS。

- [ ] **Step 3: Full verification**

```bash
./gradlew spotlessApply
./gradlew :api:test
./gradlew :application:test
```

Expected: 全部 PASS。

- [ ] **Step 4: Commit**

```bash
git add application/src/test/java/run/halo/app/extension/indexer/IndexSnapshotRecoveryIntegrationTest.java
git commit -m "test: add end-to-end index snapshot recovery integration test"
```

---

## 设计偏差记录（实现时遵守）

1. 快照相关类放在 `run.halo.app.extension.index` 包（而非设计文档的 `.index.snapshot` 子包），因为 `DefaultIndices`/`SingleValueIndex` 等均为包级可见，跨包无法访问 dump/restore。
2. 就绪超时简化为常量 30s（`DefaultIndicesManager.DEFAULT_READY_TIMEOUT`），不做配置项。
3. Task 11 无事务上下文时降级为立即应用索引变更（保持既有行为与测试兼容）；有事务时严格 afterCommit。
4. 6.1 中"afterRollback 补偿"不需要实现：索引变更只在 afterCommit 应用，rollback 天然无副作用。

