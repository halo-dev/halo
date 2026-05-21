package run.halo.app.theme.finders.impl;

import java.util.*;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.SystemConfigFetcher;
import run.halo.app.infra.SystemSetting;
import run.halo.app.infra.exception.NotFoundException;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.MenuFinder;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * A default implementation for {@link MenuFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("menuFinder")
@AllArgsConstructor
public class MenuFinderImpl implements MenuFinder {

    private final MenuTreeCache menuTreeCache;

    private final SystemConfigFetcher environmentFetcher;

    @Override
    public Mono<MenuVo> getByName(String name) {
        return listAsTree()
                .filter(menu -> menu.getMetadata().getName().equals(name))
                .next()
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Menu with name " + name + " not found")));
    }

    @Override
    public Mono<MenuVo> getPrimary() {
        return listAsTree()
                .collectList()
                .flatMap(menuVos -> {
                    if (CollectionUtils.isEmpty(menuVos)) {
                        return Mono.empty();
                    }
                    return environmentFetcher
                            .fetch(SystemSetting.Menu.GROUP, SystemSetting.Menu.class)
                            .map(SystemSetting.Menu::getPrimary)
                            .map(primaryConfig -> menuVos.stream()
                                    .filter(menuVo ->
                                            menuVo.getMetadata().getName().equals(primaryConfig))
                                    .findAny()
                                    .orElse(menuVos.get(0)))
                            .defaultIfEmpty(menuVos.get(0));
                })
                .switchIfEmpty(Mono.error(() -> new NotFoundException("No primary menu found")));
    }

    Flux<MenuVo> listAsTree() {
        return menuTreeCache.listAsTree();
    }
}
