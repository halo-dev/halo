package run.halo.app.core.endpoint.console;

import static run.halo.app.extension.index.query.Queries.equal;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Menu;
import run.halo.app.core.extension.MenuItem;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.infra.exception.NotFoundException;

@Component
@RequiredArgsConstructor
public class MenuConsoleService {

    private final ReactiveExtensionClient client;

    Mono<Menu> deleteMenu(String name) {
        return client.fetch(Menu.class, name)
                .switchIfEmpty(Mono.error(() -> new NotFoundException("Menu with name " + name + " not found")))
                .flatMap(menu ->
                        listMenuItems(name).concatMap(client::delete).then(Mono.defer(() -> client.delete(menu))));
    }

    private Flux<MenuItem> listMenuItems(String menuName) {
        var listOptions =
                ListOptions.builder().andQuery(equal("spec.menuName", menuName)).build();
        return client.listAll(MenuItem.class, listOptions, Sort.unsorted());
    }
}
