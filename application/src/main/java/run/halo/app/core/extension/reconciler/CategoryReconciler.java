package run.halo.app.core.extension.reconciler;

import static run.halo.app.extension.index.query.QueryFactory.and;
import static run.halo.app.extension.index.query.QueryFactory.equal;
import static run.halo.app.extension.index.query.QueryFactory.in;
import static run.halo.app.extension.index.query.QueryFactory.isNull;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import run.halo.app.content.permalinks.CategoryPermalinkPolicy;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Constant;
import run.halo.app.core.extension.content.Post;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.MetadataUtil;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.index.query.Query;
import run.halo.app.extension.router.selector.FieldSelector;
import run.halo.app.extension.router.selector.LabelSelector;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reconciler for {@link Category}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Component
@AllArgsConstructor
public class CategoryReconciler implements Reconciler<Reconciler.Request> {
    static final String FINALIZER_NAME = "category-protection";
    private final ExtensionClient client;
    private final CategoryPermalinkPolicy categoryPermalinkPolicy;

    @Override
    public Result reconcile(Request request) {
        return client.fetch(Category.class, request.name())
            .map(category -> {
                if (category.isDeleted()) {
                    cleanUpResourcesAndRemoveFinalizer(request.name());
                    return new Result(false, null);
                }
                addFinalizerIfNecessary(category);

                reconcileMetadata(request.name());

                reconcileStatusPermalink(request.name());

                reconcileStatusPosts(request.name());
                return new Result(true, Duration.ofMinutes(1));
            })
            .orElseGet(() -> new Result(false, null));
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new Category())
            .build();
    }

    void reconcileMetadata(String name) {
        client.fetch(Category.class, name).ifPresent(category -> {
            Map<String, String> annotations = MetadataUtil.nullSafeAnnotations(category);
            String oldPermalinkPattern = annotations.get(Constant.PERMALINK_PATTERN_ANNO);

            String newPattern = categoryPermalinkPolicy.pattern();
            annotations.put(Constant.PERMALINK_PATTERN_ANNO, newPattern);

            if (!StringUtils.equals(oldPermalinkPattern, newPattern)) {
                client.update(category);
            }
        });
    }

    private void addFinalizerIfNecessary(Category oldCategory) {
        Set<String> finalizers = oldCategory.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(FINALIZER_NAME)) {
            return;
        }
        client.fetch(Category.class, oldCategory.getMetadata().getName())
            .ifPresent(category -> {
                Set<String> newFinalizers = category.getMetadata().getFinalizers();
                if (newFinalizers == null) {
                    newFinalizers = new HashSet<>();
                    category.getMetadata().setFinalizers(newFinalizers);
                }
                newFinalizers.add(FINALIZER_NAME);
                client.update(category);
            });
    }

    private void cleanUpResourcesAndRemoveFinalizer(String categoryName) {
        client.fetch(Category.class, categoryName).ifPresent(category -> {
            if (category.getMetadata().getFinalizers() != null) {
                category.getMetadata().getFinalizers().remove(FINALIZER_NAME);
            }
            client.update(category);
        });
    }

    private void reconcileStatusPermalink(String name) {
        client.fetch(Category.class, name)
            .ifPresent(category -> {
                Category oldCategory = JsonUtils.deepCopy(category);
                category.getStatusOrDefault()
                    .setPermalink(categoryPermalinkPolicy.permalink(category));

                if (!oldCategory.equals(category)) {
                    client.update(category);
                }
            });
    }

    private void reconcileStatusPosts(String name) {
        client.fetch(Category.class, name).ifPresent(category -> {
            Category oldCategory = JsonUtils.deepCopy(category);

            populatePosts(category);

            if (!oldCategory.equals(category)) {
                client.update(category);
            }
        });
    }

    private void populatePosts(Category category) {
        String name = category.getMetadata().getName();
        Set<String> categoryNames = listChildrenByName(name)
            .stream()
            .map(item -> item.getMetadata().getName())
            .collect(Collectors.toSet());

        var totalPostCount = countTotalPosts(categoryNames);
        category.getStatusOrDefault().setPostCount(totalPostCount);

        var visiblePostCount = countVisiblePosts(categoryNames);
        category.getStatusOrDefault().setVisiblePostCount(visiblePostCount);
    }

    int countTotalPosts(Set<String> categoryNames) {
        var postListOptions = new ListOptions();
        postListOptions.setFieldSelector(FieldSelector.of(
            basePostQuery(categoryNames)
        ));
        return (int) client.listBy(Post.class, postListOptions, PageRequestImpl.ofSize(1))
            .getTotal();
    }

    int countVisiblePosts(Set<String> categoryNames) {
        var postListOptions = new ListOptions();
        var fieldQuery = and(basePostQuery(categoryNames),
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

    private static Query basePostQuery(Set<String> categoryNames) {
        return and(isNull("metadata.deletionTimestamp"),
            equal("spec.deleted", BooleanUtils.FALSE),
            in("spec.categories", categoryNames)
        );
    }

    private List<Category> listChildrenByName(String name) {
        var categories = client.listAll(Category.class, new ListOptions(), Sort.unsorted());
        Map<String, Category> nameIdentityMap = categories.stream()
            .collect(Collectors.toMap(category -> category.getMetadata().getName(),
                Function.identity()));
        final List<Category> children = new ArrayList<>();

        Deque<String> deque = new ArrayDeque<>();
        deque.add(name);
        while (!deque.isEmpty()) {
            String itemName = deque.poll();
            Category category = nameIdentityMap.get(itemName);
            if (category == null) {
                continue;
            }
            children.add(category);
            List<String> childrenNames = category.getSpec().getChildren();
            if (childrenNames != null) {
                deque.addAll(childrenNames);
            }
        }
        return children;
    }
}
