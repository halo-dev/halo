package run.halo.app.content;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.infra.utils.JsonUtils;

/**
 * A class used to update the post count of the category when the post changes.
 *
 * @author guqing
 * @since 2.15.0
 */
@Component
public class CategoryPostCountUpdater
    extends AbstractEventReconciler<CategoryPostCountUpdater.PostRelatedCategories> {

    private final CategoryPostCountService categoryPostCountService;

    public CategoryPostCountUpdater(ExtensionClient client) {
        super(CategoryPostCountUpdater.class.getName(), client);
        this.categoryPostCountService = new CategoryPostCountService(client);
    }

    @Override
    public Result reconcile(PostRelatedCategories request) {
        var categoryChanges = request.categoryChanges();

        categoryPostCountService.recalculatePostCount(categoryChanges);

        client.fetch(Post.class, request.postName()).ifPresent(post -> {
            var categories = defaultIfNull(post.getSpec().getCategories(), List.<String>of());
            var annotations = MetadataUtil.nullSafeAnnotations(post);
            var categoryAnno = JsonUtils.objectToJson(categories);
            var oldCategoryAnno = annotations.get(Post.LAST_ASSOCIATED_CATEGORIES_ANNO);

            if (!categoryAnno.equals(oldCategoryAnno)) {
                annotations.put(Post.LAST_ASSOCIATED_CATEGORIES_ANNO, categoryAnno);
                client.update(post);
            }
        });
        return Result.doNotRetry();
    }

    static class CategoryPostCountService {

        private final ExtensionClient client;

        public CategoryPostCountService(ExtensionClient client) {
            this.client = client;
        }

        public void recalculatePostCount(Collection<String> categoryNames) {
            for (String categoryName : categoryNames) {
                recalculatePostCount(categoryName);
            }
        }

        public void recalculatePostCount(String categoryName) {
            var totalPostCount = countTotalPosts(categoryName);
            var visiblePostCount = countVisiblePosts(categoryName);
            client.fetch(Category.class, categoryName).ifPresent(category -> {
                category.getStatusOrDefault().setPostCount(totalPostCount);
                category.getStatusOrDefault().setVisiblePostCount(visiblePostCount);

                client.update(category);
            });
        }

        private int countTotalPosts(String categoryName) {
            var postListOptions = new ListOptions();
            postListOptions.setFieldSelector(FieldSelector.of(
                basePostQuery(categoryName)
            ));
            return (int) client.listBy(Post.class, postListOptions, PageRequestImpl.ofSize(1))
                .getTotal();
        }

        private int countVisiblePosts(String categoryName) {
            var postListOptions = new ListOptions();
            var fieldQuery = and(basePostQuery(categoryName),
                equal("spec.visible", Post.VisibleEnum.PUBLIC.name())
            );
            var labelSelector = LabelSelector.builder()
                .eq(Post.PUBLISHED_LABEL, BooleanUtils.TRUE)
                .build();
            postListOptions.setFieldSelector(FieldSelector.of(fieldQuery));
            postListOptions.setLabelSelector(labelSelector);
            return (int) client.listBy(Post.class, postListOptions, PageRequestImpl.ofSize(1))
                .getTotal();
        }

        private static Query basePostQuery(String categoryName) {
            return and(isNull("metadata.deletionTimestamp"),
                equal("spec.deleted", BooleanUtils.FALSE),
                equal("spec.categories", categoryName)
            );
        }
    }

    public record PostRelatedCategories(String postName, Collection<String> categoryChanges) {
    }

    @EventListener(PostUpdatedEvent.class)
    public void onPostUpdated(PostUpdatedEvent event) {
        var postName = event.getName();
        var changes = calcCategoriesToUpdate(event.getName());
        queue.addImmediately(new PostRelatedCategories(postName, changes));
    }

    @EventListener(PostDeletedEvent.class)
    public void onPostDeleted(PostDeletedEvent event) {
        var postName = event.getName();
        var categories = defaultIfNull(event.getPost().getSpec().getCategories(),
            List.<String>of());
        queue.addImmediately(new PostRelatedCategories(postName, categories));
    }

    private Set<String> calcCategoriesToUpdate(String postName) {
        return client.fetch(Post.class, postName)
            .map(post -> {
                var annotations = MetadataUtil.nullSafeAnnotations(post);
                var oldCategories =
                    Optional.ofNullable(annotations.get(Post.LAST_ASSOCIATED_CATEGORIES_ANNO))
                        .filter(StringUtils::isNotBlank)
                        .map(categoriesJson -> JsonUtils.jsonToObject(categoriesJson,
                            String[].class))
                        .orElse(new String[0]);

                Set<String> categoriesToUpdate = Sets.newHashSet(oldCategories);
                var newCategories = post.getSpec().getCategories();
                if (newCategories != null) {
                    categoriesToUpdate.addAll(newCategories);
                }
                return categoriesToUpdate;
            })
            .orElse(Set.of());
    }
}
