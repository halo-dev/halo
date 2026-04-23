package run.halo.app.core.reconciler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.core.extension.MenuItem.MenuItemSpec;
import run.halo.app.core.extension.MenuItem.MenuItemStatus;
import run.halo.app.core.extension.content.Category;
import run.halo.app.core.extension.content.Post;
import run.halo.app.core.extension.content.SinglePage;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.event.post.CategoryUpdatedEvent;
import run.halo.app.event.post.PostDeletedEvent;
import run.halo.app.event.post.PostUpdatedEvent;
import run.halo.app.event.post.SinglePageUpdatedEvent;
import run.halo.app.event.post.TagUpdatedEvent;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Ref;
import run.halo.app.extension.controller.Controller;
import run.halo.app.extension.controller.ControllerBuilder;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.extension.controller.Reconciler.Request;
import run.halo.app.extension.index.query.Queries;

@Slf4j
@Component
class MenuItemReconciler implements Reconciler<Request> {

    private final ExtensionClient client;

    private final ReactiveExtensionClient reactiveClient;

    public MenuItemReconciler(ExtensionClient client, ReactiveExtensionClient reactiveClient) {
        this.client = client;
        this.reactiveClient = reactiveClient;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(MenuItem.class, request.name())
            .ifPresent(menuItem -> {
                if (ExtensionUtil.isDeleted(menuItem)) {
                    return;
                }
                if (menuItem.getSpec() == null) {
                    menuItem.setSpec(new MenuItemSpec());
                }
                var spec = menuItem.getSpec();
                var targetRef = spec.getTargetRef();
                if (targetRef != null) {
                    if (Ref.groupKindEquals(targetRef, Category.GVK)) {
                        handleCategoryRef(menuItem, targetRef.getName());
                    } else if (Ref.groupKindEquals(targetRef, Tag.GVK)) {
                        handleTagRef(menuItem, targetRef.getName());
                    } else if (Ref.groupKindEquals(targetRef, SinglePage.GVK)) {
                        handleSinglePageSpec(menuItem, targetRef.getName());
                    } else if (Ref.groupKindEquals(targetRef, Post.GVK)) {
                        handlePostRef(menuItem, targetRef.getName());
                    } else {
                        // Do nothing while the targetRef is not supported, just log an error and
                        // reset the status.
                        log.error("Unsupported MenuItem targetRef " + targetRef);
                        resetStatus(menuItem);
                    }
                } else {
                    if (menuItem.getStatus() == null) {
                        menuItem.setStatus(new MenuItemStatus());
                    }
                    var status = menuItem.getStatus();
                    status.setHref(spec.getHref());
                    status.setDisplayName(spec.getDisplayName());
                }
                client.update(menuItem);
            });
        return Result.doNotRetry();
    }

    private void handlePostRef(MenuItem menuItem, String postName) {
        var post = client.fetch(Post.class, postName).orElse(null);
        if (post == null || ExtensionUtil.isDeleted(post)) {
            resetStatus(menuItem);
            return;
        }
        if (menuItem.getStatus() == null) {
            menuItem.setStatus(new MenuItemStatus());
        }
        var status = menuItem.getStatus();
        status.setHref(Optional.ofNullable(post.getStatus())
            .map(Post.PostStatus::getPermalink)
            .orElse(null));
        status.setDisplayName(Optional.ofNullable(post.getSpec())
            .map(Post.PostSpec::getTitle)
            .orElse(null));
    }

    private void handleSinglePageSpec(MenuItem menuItem, String singlePageName) {
        var singlePage = client.fetch(SinglePage.class, singlePageName).orElse(null);
        if (singlePage == null || ExtensionUtil.isDeleted(singlePage)) {
            resetStatus(menuItem);
            return;
        }
        if (menuItem.getStatus() == null) {
            menuItem.setStatus(new MenuItemStatus());
        }
        var status = menuItem.getStatus();
        status.setHref(Optional.ofNullable(singlePage.getStatus())
            .map(SinglePage.SinglePageStatus::getPermalink)
            .orElse(null));
        status.setDisplayName(Optional.ofNullable(singlePage.getSpec())
            .map(SinglePage.SinglePageSpec::getTitle)
            .orElse(null));
    }

    private void handleTagRef(MenuItem menuItem, String tagName) {
        var tag = client.fetch(Tag.class, tagName).orElse(null);
        if (tag == null || ExtensionUtil.isDeleted(tag)) {
            resetStatus(menuItem);
            return;
        }
        if (menuItem.getStatus() == null) {
            menuItem.setStatus(new MenuItemStatus());
        }
        var status = menuItem.getStatus();
        status.setHref(Optional.ofNullable(tag.getStatus())
            .map(Tag.TagStatus::getPermalink)
            .orElse(null));
        status.setDisplayName(Optional.ofNullable(tag.getSpec())
            .map(Tag.TagSpec::getDisplayName)
            .orElse(null));
    }

    @Override
    public Controller setupWith(ControllerBuilder builder) {
        return builder
            .extension(new MenuItem())
            .build();
    }

    @EventListener
    Mono<Void> onCategoryUpdated(CategoryUpdatedEvent event) {
        log.debug("Received CategoryUpdatedEvent for category {}",
            event.getCategory().getMetadata().getName());
        var category = event.getCategory();
        return findMenuItemsByRef(Ref.of(category))
            .doOnNext(MenuItemReconciler::requestUpdate)
            .flatMap(reactiveClient::update)
            .then();
    }

    @EventListener
    Mono<Void> onTagUpdated(TagUpdatedEvent event) {
        log.debug("Received TagUpdatedEvent for tag {}", event.getTag().getMetadata().getName());
        var tag = event.getTag();
        return findMenuItemsByRef(Ref.of(tag))
            .doOnNext(MenuItemReconciler::requestUpdate)
            .flatMap(reactiveClient::update)
            .then();
    }

    @EventListener
    Mono<Void> onSinglePageUpdated(SinglePageUpdatedEvent event) {
        log.debug("Received SinglePageUpdatedEvent for single page {}",
            event.getSinglePage().getMetadata().getName());
        var singlePage = event.getSinglePage();
        return findMenuItemsByRef(Ref.of(singlePage))
            .doOnNext(MenuItemReconciler::requestUpdate)
            .flatMap(reactiveClient::update)
            .then();
    }

    @EventListener
    Mono<Void> onPostUpdated(PostUpdatedEvent event) {
        log.debug("Received PostUpdatedEvent for post {}", event.getName());
        var postName = event.getName();
        return findMenuItemsByRef(Ref.of(postName, Post.GVK))
            .doOnNext(MenuItemReconciler::requestUpdate)
            .flatMap(reactiveClient::update)
            .then();
    }

    @EventListener
    Mono<Void> onPostDeleted(PostDeletedEvent event) {
        log.debug("Received PostDeletedEvent for post {}", event.getName());
        var post = event.getPost();
        return findMenuItemsByRef(Ref.of(post))
            .doOnNext(MenuItemReconciler::requestUpdate)
            .flatMap(reactiveClient::update)
            .then();
    }

    private Flux<MenuItem> findMenuItemsByRef(Ref ref) {
        var listOptions = ListOptions.builder()
            .andQuery(Queries.equal("spec.targetRef", Ref.toIdentifier(ref)))
            .build();
        return reactiveClient.listAll(MenuItem.class, listOptions, Sort.unsorted());
    }

    private static void requestUpdate(MenuItem menuItem) {
        var metadata = menuItem.getMetadata();
        if (metadata.getAnnotations() == null) {
            metadata.setAnnotations(new HashMap<>());
        }
        metadata.getAnnotations()
            .put(MenuItem.REQUEST_TO_UPDATE_ANNO, Instant.now().toString());
    }

    private static void resetStatus(MenuItem menuItem) {
        Optional.ofNullable(menuItem.getStatus()).ifPresent(status -> {
            status.setHref(null);
            status.setDisplayName(null);
        });
    }

    private void handleCategoryRef(MenuItem menuItem, String categoryName) {
        var category = client.fetch(Category.class, categoryName).orElse(null);
        if (category == null || ExtensionUtil.isDeleted(category)) {
            resetStatus(menuItem);
            return;
        }
        var status = menuItem.getStatus();
        if (status == null) {
            status = new MenuItemStatus();
        }
        status.setHref(Optional.ofNullable(category.getStatus())
            .map(Category.CategoryStatus::getPermalink)
            .orElse(null));
        status.setDisplayName(Optional.ofNullable(category.getSpec())
            .map(Category.CategorySpec::getDisplayName)
            .orElse(null));
    }

}
