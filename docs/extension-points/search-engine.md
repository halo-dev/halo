# 搜索引擎扩展点

随着 Halo 的不断发展，搜索引擎模块也逐渐完善。搜索引擎模块是 Halo 的核心模块之一，它负责为 Halo
提供全文搜索功能。搜索引擎模块目前仅支持本地全文搜索引擎 [Lucene](https://lucene.apache.org/)，其他搜索引擎的支持，如
Solr、MeiliSearch 或 ElasticSearch，需要通过插件来实现。

搜索引擎模块包含两个扩展点，分别是搜索引擎扩展和搜索文档扩展。搜索引擎扩展主要负责索引文档的添加、更新、删除和重建，搜索文档扩展则主要用于扩展文档类型，不仅限于文章类型。

从 Halo 2.17 开始，Halo 利用事件机制收集来自核心和插件中所发布的文档，其中也包含了文档类型用于区分。所以插件中可以通过发布事件的方式来控制文档的添加、更新、删除和重建，重建操作所需要的数据则由搜索文档扩展提供。

## 搜索引擎扩展（`run.halo.app.search.SearchEngine`）

如果插件想要扩展搜索引擎，如 Solr、MeiliSearch 或者 ElasticSearch，可以通过实现 `SearchEngine` 接口来实现。

具体实现可参考 Halo 的 Lucene 搜索引擎实现：`run.halo.app.search.LuceneSearchEngine`。

## 搜索文档扩展（`run.halo.app.search.HaloDocumentsProvider`）

如果插件想要扩展搜索文档类型，可以通过实现 `HaloDocumentsProvider` 接口来实现。具体实现可参考 Halo
的默认实现：`run.halo.app.search.post.PostHaloDocumentsProvider`。

- 添加文档示例如下所示

  ```java
  class HaloDocumentAddExample {

    private final ApplicationEventPublisher eventPublisher;

    void addDocuments() {
      // concrete Halo documents
      List<HaloDocument> documents = ...;
      eventPublisher.publishEvent(new HaloDocumentAddRequestEvent(this, documents));
    }
  }
  ```

- 删除文档示例如下所示

  ```java
  class HaloDocumentDeleteExample {

    private final ApplicationEventPublisher eventPublisher;

    void deleteDocuments() {
      Set<String> docIds = ...;
      eventPublisher.publishEvent(new HaloDocumentDeleteRequestEvent(this, docIds));
    }
  }
  ```

- 重建索引示例如下所示：

  ```java
  class HaloDocumentRebuildExample {

    private final ApplicationEventPublisher eventPublisher;

    void rebuildDocument() {
      eventPublisher.publishEvent(new HaloDocumentRebuildRequestEvent(this));
    }

  }
  ```
  