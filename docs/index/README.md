# 索引机制 RFC

## 背景

目前 Halo 使用 Extension 机制来存储和获取数据以便支持更好的扩展性，所以设计之初就存在查询数据时会将对应 Extension 的所有数据查询到内存中处理的问题，这会导致当分页查询和条件查询时也会有大批量无效数据被加载到内存中，随着 Halo 用户的数据量的增长，如果没有一个方案来解决这样的数据查询问题会对 Halo 用户的服务器内存资源有较高的要求，因此本篇提出使用索引机制来解决数据查询问题，以便提高查询效率和减少内存开销。

## 目标

- **提高查询效率**：加快数据检索速度。通过使用索引，数据库可以快速定位到数据行的位置，从而减少必须读取的数据量。
- **减少网络和内存开销：** 没有索引前查询数据会将 Extension 的所有数据都传输到应用对网络和内存开销都很大，通过索引定位确切的数据来减少不必要的消耗。
- **优化排序操作**：通过索引加速排序操作，因此需要索引本身有序。
- **索引存储可扩展**：索引虽然能提高查询效率，但它会占用额外的存储空间，如果过大可以考虑在磁盘上读写等方式来减少对内存的占用。

## 非目标

- 索引的持久化存储，前期只考虑在内存中存储索引，如果后续索引过大可以考虑在磁盘上读写等方式来减少对内存的占用。
- 索引的自动维护，索引的维护需要考虑到索引的数据是否改变，如果改变则需要更新索引，这个改变的判断不好界定，所以先不考虑索引的自动维护。
- 索引的前置验证，比如在启动时验证索引的完整性和正确性，但目前每次启动都会重新构建索引，所以先不考虑索引的前置验证。
- 多线程构建索引，目前索引的构建是单线程的，如果后续索引过大可以考虑多线程构建索引。

## 方案

索引是一种存储数据结构，可提供对数据集中字段的高效查找。索引将 Extension 中的字段映射到 Extension 的名称，以便在查询特定字段时不需要完整的扫描。

### 索引结构

每个 Extension 声明的索引都会被创建为一个 keySpace 与索引信息的映射，
类如对附件分组的一个对名称的索引示例如下：

```javascript
{
 "/registry/storage.halo.run/groups": [{
  name: "specName",
  spec: {
    // a function that returns the value of the index key
    indexFunc: function(doc) {
      return doc.spec.name;
    },
    order: 'asc',
    unique: false
  },
  v: 1,
  ready: false
 },
 {
  name: "metadata.labels",
  spec: {
    indexFunc: function(doc) {
       var labels = obj.getMetadata().getLabels();
        if (labels == null) {
          return Set.of();
        }
        return labels.entrySet()
          .stream()
          .map(entry -> entry.getKey() + "=" + entry.getValue())
          .collect(Collectors.toSet());
    },
    order: 'asc',
    unique: false
  },
  v: 1,
  ready: true,
 }]
}
```

- `name: specName` 表示索引的名称，每个 Extension 声明的索引名称不能重复，通常为字段路径如 `metadata.name`。
- `spec.indexFunc` 用于生成索引键，索引键是一个字符串数组，每个字符串都是一个索引键值，索引键值是一个字符串。
- `spec.order` 用于记录索引键的排序方式，可选值为 `asc` 或 `desc`，`asc` 表示升序，`desc` 表示降序。
- `spec.unique` 用于标识是否为唯一索引以在添加索引时进行唯一性检查。
- `v` 用于记录索引结构的版本以防止后续为优化导致索引结构改变时便于检查重建索引。
- `ready` 用于记录该索引是否构建完成，当开始构建该索引键索引记录时为 false，如果构建完成则修改为 true，如果因为断电等导致索引构建不完整则 ready 会是 false，下次启动时需要重新开始构建。

对于每个 Extension 都有一个默认的唯一索引 `metadata.name` 其 entries 与 Extension 每一条记录唯一对应。

### 索引构建

索引是通过对 Extension 数据执行完整扫描来构建的。

1. **针对特定 Extension 数据集的操作**: 当构建索引时，操作是针对特定的 Extension 数据进行的。将 `ready` 置为 `false`
2. **扫描 Extension 数据集**: 构建索引的关键步骤是扫描 Extension 数据集中的每一条记录。这个扫描过程并不是基于数据库中所有数据的顺序，而是专注于该 Extension 数据集内的数据。当构建索引时会锁定对该 Extension 的写操作。
3. **生成索引键（KeyString键）**:对于 Extension 数据集中的每个 Extension，会根据其索引字段生成 KeyString 键。String 为 Extension 的 `metadata.name` 用于定位 Extension 在数据库中的位置。
4. **排序和插入操作**: 生成的键会被插入到一个外部排序器中，以确保它们的顺序。排序后，这些键按顺序批量加载到索引中。
5. 释放对该 Extension 写操作的锁定完成了索引构建。

对于后续 Extension 和索引的更新需要在同一个事务中以确保一致性。

```json
{
  "metadata.name": {
    "group-1": []
  },
  "specName": {
    "zhangsan": [
        "metadata-name-1"
    ],
    "lisi": [
        "metadata-name-2"
    ]
  },
  "halo.run/hidden": {
    "true": [
        "metadata-name-3"
    ],
    "false": [
        "metadata-name-4"
    ]
  }
}
```

### 索引前置验证

1. 每次启动后先检查索引是否 ready
2. `metadata.name` 索引条目的数量始终与数据库中记录的 Extension 数量一致
3. 如果排序顺序为升序，则索引条目按递增顺序排列。
4. 如果排序顺序为降序，则索引条目按降序排列。
5. 每个索引的索引条目数量不超过数据库中记录的对应 Extension 数量。

### 索引在 Extension 的声明

手动注册索引

```java
public class IndexSpec {
    private String name;

    private IndexAttribute indexFunc;

    private OrderType order;

    private boolean unique;

    public enum OrderType {
        ASC,
        DESC
    }

    // Getters and other methods...
}

IndexSpecs indexSpecs = indexSpecRegistry.indexFor(Person.class);
indexSpecs.add(new IndexSpec()
    .setName("spec.name")
    .setOrder(IndexSpec.OrderType.DESC)
    .setIndexFunc(IndexAttributeFactory.simpleAttribute(Person.class,
        e -> e.getSpec().getName())
    )
    .setUnique(false));
```

用于普通索引的注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE}) // 用于类和注解的注解
public @interface Index {
  String name(); // 索引名称
  String field(); // 需要索引的字段
}
```

Indexes 注解用于声明多个索引

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Indexes {
    Index[] value() default {};  // Index注解数组
}
```

```java
@Data
@Indexes({
  @Index(name = "specName", field = "spec.name"),
  @Index(name = "creationTimestamp", field = "metadata.creationTimestamp"),
})
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "my-plugin.guqing.io",
        version = "v1alpha1",
        kind = "Person",
        plural = "persons",
        singular = "person")
public class Person extends Extension {

    @Schema(description = "The description on name field", maxLength = 100)
    private String name;

    @Schema(description = "The description on age field", maximum = "150", minimum = "0")
    private Integer age;

    @Schema(description = "The description on gender field")
    private Gender gender;

    public enum Gender {
        MALE, FEMALE,
    }
}
```

不论是手动注册索引还是通过注解注册索引都由 IndexSpecRegistry 管理。

```java
public interface IndexSpecRegistry {
    /**
     * <p>Create a new {@link IndexSpecs} for the given {@link Extension} type.</p>
     * <p>The returned {@link IndexSpecs} is always includes some default {@link IndexSpec} that
     * does not need to be registered again:</p>
     * <ul>
     *     <li>{@link Metadata#getName()} for unique primary index spec named metadata_name</li>
     *     <li>{@link Metadata#getCreationTimestamp()} for creation_timestamp index spec</li>
     *     <li>{@link Metadata#getDeletionTimestamp()} for deletion_timestamp index spec</li>
     *     <li>{@link Metadata#getLabels()} for labels index spec</li>
     * </ul>
     *
     * @param extensionType must not be {@literal null}.
     * @param <E> the extension type
     * @return the {@link IndexSpecs} for the given {@link Extension} type.
     */
    <E extends Extension> IndexSpecs indexFor(Class<E> extensionType);

    /**
     * Get {@link IndexSpecs} for the given {@link Extension} type registered before.
     *
     * @param extensionType must not be {@literal null}.
     * @param <E> the extension type
     * @return the {@link IndexSpecs} for the given {@link Extension} type.
     * @throws IllegalArgumentException if no {@link IndexSpecs} found for the given
     *                                  {@link Extension} type.
     */
    <E extends Extension> IndexSpecs getIndexSpecs(Class<E> extensionType);

    boolean contains(Class<? extends Extension> extensionType);

    void removeIndexSpecs(Class<? extends Extension> extensionType);

    /**
     * Get key space for an extension type.
     *
     * @param scheme is a scheme of an Extension.
     * @return key space(never null)
     */
    @NonNull
    String getKeySpace(Scheme scheme);
}
```

对于添加了索引的 Extension 可以使用 `IndexedQueryEngine` 来查询数据：

```java
public interface IndexedQueryEngine {
   /**
     * Page retrieve the object records by the given {@link GroupVersionKind} and
     * {@link ListOptions}.
     *
     * @param type the type of the object must exist in
     * {@link run.halo.app.extension.SchemeManager}.
     * @param options the list options to use for retrieving the object records.
     * @param page which page to retrieve and how large the page should be.
     * @return a collection of {@link Metadata#getName()} for the given page.
     */
    ListResult<String> retrieve(GroupVersionKind type, ListOptions options, PageRequest page);

    /**
     * Retrieve all the object records by the given {@link GroupVersionKind} and
     * {@link ListOptions}.
     *
     * @param type the type of the object must exist in {@link run.halo.app.extension.SchemeManager}
     * @param options the list options to use for retrieving the object records
     * @return a collection of {@link Metadata#getName()}
     */
    List<String> retrieveAll(GroupVersionKind type, ListOptions options);
}
```

但为了简便起见，会在 ReactiveExtensionClient 中提供一个 `listBy` 方法来查询数据：

```java
public interface ReactiveExtensionClient {
  //...
  <E extends Extension> Mono<ListResult<E>> listBy(Class<E> type, ListOptions options,
        PageRequest pageable);
}
```

其中 `ListOptions` 包含两部分，`LabelSelector` 和 `FieldSelector`，一个常见的手动构建的 `ListOptions` 示例：

```java
var listOptions = new ListOptions();
listOptions.setLabelSelector(LabelSelector.builder()
  .eq("key1", "value1").build());
listOptions.setFieldSelector(FieldSelector.builder()
  .eq("slug", "slug1").build());
```

为了兼容以前的写法，对于 APIs 中可以继续使用 `run.halo.app.extension.router.IListRequest`，然后使用工具类转换即可得到 `ListOptions` 和 `PageRequest`。

```java
class QueryListRequest implements IListRequest {
    public ListOptions toListOptions() {
        return labelAndFieldSelectorToListOptions(getLabelSelector(), getFieldSelector());
    }

    public PageRequest toPageRequest() {
        return PageRequestImpl.of(getPage(), getSize(), getSort());
    }
}
```

### Reconciler

对于 Reconciler 来说，之前每次由 DefaultController 启动对于需要 `syncAllOnStart` 的 Reconciler 都是获取所有对应的 Extension 数据，然后再进行 Reconcile，这样会导致每次都将所有的 Extension 数据加载到内存中，随着数据量的增加导致内存占用过大，当有了索引后只获取所有 Extension 的 `metadata.name` 来触发 reconcile 即可。

GcReconciler 也从索引中获取 `metadata.deletionTimestamp` 不为空的 Extension 的 `metadata.name` 来触发 reconcile 以减少全量加载数据的操作。
