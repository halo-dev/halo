package run.halo.app.theme.finders.impl;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.event.menu.MenuItemReconciledEvent;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.Watcher;
import run.halo.app.theme.finders.vo.MenuItemVo;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * Caches menu tree data used by the theme menu finder.
 *
 * @author ryanwang
 * @since 2.24.0
 */
@Slf4j
@Component
class MenuTreeCache implements Watcher {

    static final String CACHE_NAME = "menu.tree";

    static final String CACHE_KEY = "all";

    private final ReactiveExtensionClient client;

    private final CacheManager cacheManager;

    private final AtomicReference<Mono<List<MenuVo>>> loading = new AtomicReference<>();

    private final AtomicLong invalidationVersion = new AtomicLong();

    private volatile boolean disposed;

    private Runnable disposeHook;

    MenuTreeCache(ReactiveExtensionClient client, CacheManager cacheManager) {
        this.client = client;
        this.cacheManager = cacheManager;
    }

    @PostConstruct
    void startWatching() {
        client.watch(this);
    }

    @PreDestroy
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        if (disposeHook != null) {
            disposeHook.run();
        }
    }

    Flux<MenuVo> listAsTree() {
        return Mono.defer(this::getOrLoad).flatMapMany(Flux::fromIterable);
    }

    void evict() {
        invalidationVersion.incrementAndGet();
        loading.set(null);
        menuCache().ifPresent(cache -> cache.evictIfPresent(CACHE_KEY));
        log.debug("Evicted {} cache", CACHE_NAME);
    }

    @Override
    public void onAdd(Extension extension) {
        evictIfMenuRelated(extension);
    }

    @Override
    public void onUpdate(Extension oldExtension, Extension newExtension) {
        if (newExtension instanceof Menu) {
            evict();
            return;
        }
        if (oldExtension instanceof MenuItem oldMenuItem
                && newExtension instanceof MenuItem newMenuItem
                && menuItemTreeChanged(oldMenuItem, newMenuItem)) {
            evict();
        }
    }

    @Override
    public void onDelete(Extension extension) {
        evictIfMenuRelated(extension);
    }

    @EventListener
    void onMenuItemReconciled(MenuItemReconciledEvent event) {
        evict();
    }

    @Override
    public void registerDisposeHook(Runnable dispose) {
        this.disposeHook = dispose;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    private Mono<List<MenuVo>> getOrLoad() {
        var cachedMenuTree = getCachedMenuTree();
        if (cachedMenuTree != null) {
            return Mono.just(cachedMenuTree);
        }
        var existingLoading = loading.get();
        if (existingLoading != null) {
            return existingLoading;
        }
        var currentVersion = invalidationVersion.get();
        var newLoading = newLoading(currentVersion);
        if (loading.compareAndSet(null, newLoading)) {
            return newLoading;
        }
        var currentLoading = loading.get();
        return currentLoading == null ? getOrLoad() : currentLoading;
    }

    private Mono<List<MenuVo>> newLoading(long version) {
        var loadingRef = new AtomicReference<Mono<List<MenuVo>>>();
        var mono = loadMenuTree()
                .doOnNext(menuTree -> {
                    if (invalidationVersion.get() == version) {
                        putCachedMenuTree(menuTree);
                    }
                })
                .doFinally(signalType -> loading.compareAndSet(loadingRef.get(), null))
                .cache();
        loadingRef.set(mono);
        return mono;
    }

    private Mono<List<MenuVo>> loadMenuTree() {
        return listAllMenuItem()
                .collectList()
                .map(MenuTreeCache::populateParentName)
                .flatMapMany(menuItemVos -> {
                    List<MenuItemVo> treeList = listToTree(menuItemVos);
                    Map<String, MenuItemVo> nameItemRootNodeMap = treeList.stream()
                            .collect(Collectors.toMap(item -> item.getMetadata().getName(), Function.identity()));
                    return listAll().map(menuVo -> {
                        LinkedHashSet<String> menuItemNames = menuVo.getSpec().getMenuItems();
                        if (menuItemNames == null) {
                            return menuVo.withMenuItems(List.of());
                        }
                        List<MenuItemVo> menuItems = menuItemNames.stream()
                                .map(nameItemRootNodeMap::get)
                                .filter(Objects::nonNull)
                                .sorted(defaultTreeNodeComparator())
                                .toList();
                        return menuVo.withMenuItems(menuItems);
                    });
                })
                .collectList();
    }

    private Flux<MenuVo> listAll() {
        return client.list(Menu.class, null, null).map(MenuVo::from);
    }

    private Flux<MenuItemVo> listAllMenuItem() {
        return client.list(MenuItem.class, null, null).map(MenuItemVo::from);
    }

    private void evictIfMenuRelated(Extension extension) {
        if (extension instanceof Menu || extension instanceof MenuItem) {
            evict();
        }
    }

    private boolean menuItemTreeChanged(MenuItem oldMenuItem, MenuItem newMenuItem) {
        return !Objects.equals(oldMenuItem.getSpec(), newMenuItem.getSpec())
                || !Objects.equals(oldMenuItem.getStatus(), newMenuItem.getStatus());
    }

    private Optional<Cache> menuCache() {
        return Optional.ofNullable(cacheManager.getCache(CACHE_NAME));
    }

    @SuppressWarnings("unchecked")
    private List<MenuVo> getCachedMenuTree() {
        return menuCache()
                .map(cache -> cache.get(CACHE_KEY))
                .map(Cache.ValueWrapper::get)
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .map(list -> (List<MenuVo>) list)
                .orElse(null);
    }

    private void putCachedMenuTree(List<MenuVo> menuTree) {
        menuCache().ifPresent(cache -> cache.put(CACHE_KEY, List.copyOf(menuTree)));
    }

    static List<MenuItemVo> listToTree(Collection<MenuItemVo> list) {
        Map<String, List<MenuItemVo>> parentNameIdentityMap = list.stream()
                .filter(menuItemVo -> menuItemVo.getParentName() != null)
                .collect(Collectors.groupingBy(MenuItemVo::getParentName));

        list.forEach(node -> {
            List<MenuItemVo> children =
                    parentNameIdentityMap.getOrDefault(node.getMetadata().getName(), List.of()).stream()
                            .sorted(defaultTreeNodeComparator())
                            .toList();
            node.setChildren(children);
        });

        return list.stream().filter(v -> v.getParentName() == null).collect(Collectors.toList());
    }

    static Comparator<MenuItemVo> defaultTreeNodeComparator() {
        Function<MenuItemVo, Integer> priority = menuItem -> menuItem.getSpec().getPriority();
        Function<MenuItemVo, Instant> createTime =
                menuItem -> menuItem.getMetadata().getCreationTimestamp();
        Function<MenuItemVo, String> name = menuItem -> menuItem.getMetadata().getName();

        return Comparator.comparing(priority)
                .thenComparing(createTime, Comparators.nullsLow())
                .thenComparing(name);
    }

    static Collection<MenuItemVo> populateParentName(List<MenuItemVo> menuItemVos) {
        Map<String, MenuItemVo> nameIdentityMap = menuItemVos.stream()
                .collect(Collectors.toMap(menuItem -> menuItem.getMetadata().getName(), Function.identity()));

        nameIdentityMap.forEach((name, value) -> {
            LinkedHashSet<String> children = value.getSpec().getChildren();
            if (children == null) {
                return;
            }
            for (String child : children) {
                MenuItemVo childNode = nameIdentityMap.get(child);
                if (childNode != null) {
                    childNode.setParentName(name);
                }
            }
        });
        return nameIdentityMap.values();
    }
}
