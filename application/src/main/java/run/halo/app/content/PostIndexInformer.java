package run.halo.app.content;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.GroupVersionKind;
import run.halo.app.extension.Metadata;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.Unstructured;
import run.halo.app.extension.Watcher;
import run.halo.app.extension.controller.RequestSynchronizer;
import run.halo.app.infra.SchemeInitializedEvent;

/**
 * <p>Monitor changes to {@link Post} resources and establish a local, in-memory cache in an
 * Indexer.
 * When changes to posts are detected, the Indexer is updated using the indexFunc to maintain
 * its integrity.
 * This enables quick retrieval of the unique identifier(It is usually {@link Metadata#getName()})
 * for article objects using the getByIndex method when needed.</p>
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
public class PostIndexInformer implements ApplicationListener<SchemeInitializedEvent>,
    DisposableBean {
    public static final String TAG_POST_INDEXER = "tag-post-indexer";
    public static final String LABEL_INDEXER_NAME = "post-label-indexer";

    private final RequestSynchronizer synchronizer;

    private final Indexer<Post> postIndexer;

    private final PostWatcher postWatcher;

    public PostIndexInformer(ExtensionClient client) {
        postIndexer = new DefaultIndexer<>();
        postIndexer.addIndexFunc(TAG_POST_INDEXER, post -> {
            List<String> tags = post.getSpec().getTags();
            return tags != null ? Set.copyOf(tags) : Set.of();
        });
        postIndexer.addIndexFunc(LABEL_INDEXER_NAME, labelIndexFunc());

        this.postWatcher = new PostWatcher();
        this.synchronizer = new RequestSynchronizer(true,
            client,
            new Post(),
            postWatcher,
            this::checkExtension);
    }

    private DefaultIndexer.IndexFunc<Post> labelIndexFunc() {
        return post -> {
            Map<String, String> labels = MetadataUtil.nullSafeLabels(post);
            Set<String> indexKeys = new HashSet<>();
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                indexKeys.add(labelKey(entry.getKey(), entry.getValue()));
            }
            return indexKeys;
        };
    }

    public Set<String> getByIndex(String indexName, String indexKey) {
        return postIndexer.getByIndex(indexName, indexKey);
    }

    public Set<String> getByTagName(String tagName) {
        return postIndexer.getByIndex(TAG_POST_INDEXER, tagName);
    }

    public Set<String> getByLabels(Map<String, String> labels) {
        if (labels == null) {
            return Set.of();
        }
        Set<String> result = new HashSet<>();
        for (Map.Entry<String, String> entry : labels.entrySet()) {
            Set<String> values = postIndexer.getByIndex(LABEL_INDEXER_NAME,
                labelKey(entry.getKey(), entry.getValue()));
            if (values == null) {
                // No objects have this label, no need to continue searching
                return Set.of();
            }
            if (result.isEmpty()) {
                result.addAll(values);
            } else {
                result.retainAll(values);
            }
        }
        return result;
    }

    String labelKey(String labelName, String labelValue) {
        return labelName + "=" + labelValue;
    }

    public Set<String> getByLabel(String labelName, String labelValue) {
        return postIndexer.getByIndex(LABEL_INDEXER_NAME, labelKey(labelName, labelValue));
    }

    @Override
    public void destroy() throws Exception {
        if (postWatcher != null) {
            postWatcher.dispose();
        }
        if (synchronizer != null) {
            synchronizer.dispose();
        }
    }

    @Override
    public void onApplicationEvent(@NonNull SchemeInitializedEvent event) {
        if (!synchronizer.isStarted()) {
            synchronizer.start();
        }
    }

    class PostWatcher implements Watcher {
        private Runnable disposeHook;
        private boolean disposed = false;
        private final StampedLock lock = new StampedLock();

        @Override
        public void onAdd(Extension extension) {
            if (!checkExtension(extension)) {
                return;
            }
            handleIndicates(extension, postIndexer::add);
        }

        @Override
        public void onUpdate(Extension oldExt, Extension newExt) {
            if (!checkExtension(newExt)) {
                return;
            }
            handleIndicates(newExt, postIndexer::update);
        }

        @Override
        public void onDelete(Extension extension) {
            if (!checkExtension(extension)) {
                return;
            }
            handleIndicates(extension, postIndexer::delete);
        }

        @Override
        public void registerDisposeHook(Runnable dispose) {
            this.disposeHook = dispose;
        }

        @Override
        public void dispose() {
            if (isDisposed()) {
                return;
            }
            this.disposed = true;
            if (this.disposeHook != null) {
                this.disposeHook.run();
            }
        }

        @Override
        public boolean isDisposed() {
            return this.disposed;
        }

        void handleIndicates(Extension extension, BiConsumer<String, Post> consumer) {
            Post post = convertTo(extension);
            Set<String> indexNames = getIndexNames();
            for (String indexName : indexNames) {
                maintainIndicates(indexName, post, consumer);
            }
        }

        Set<String> getIndexNames() {
            long stamp = lock.tryOptimisticRead();
            Set<String> indexNames = postIndexer.indexNames();
            if (!lock.validate(stamp)) {
                stamp = lock.readLock();
                try {
                    return postIndexer.indexNames();
                } finally {
                    lock.unlockRead(stamp);
                }
            }
            return indexNames;
        }

        void maintainIndicates(String indexName, Post post, BiConsumer<String, Post> consumer) {
            long stamp = lock.writeLock();
            try {
                consumer.accept(indexName, post);
            } finally {
                lock.unlockWrite(stamp);
            }
        }
    }

    private Post convertTo(Extension extension) {
        if (extension instanceof Post) {
            return (Post) extension;
        }
        return Unstructured.OBJECT_MAPPER.convertValue(extension, Post.class);
    }

    private boolean checkExtension(Extension extension) {
        return !postWatcher.isDisposed()
            && extension.getMetadata().getDeletionTimestamp() == null
            && isPost(extension);
    }

    private boolean isPost(Extension extension) {
        return GroupVersionKind.fromExtension(Post.class).equals(extension.groupVersionKind());
    }
}
