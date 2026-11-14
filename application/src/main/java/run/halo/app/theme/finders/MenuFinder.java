package run.halo.app.theme.finders;

import java.util.Collection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.theme.finders.vo.MenuVo;

/**
 * A finder for {@link run.halo.app.core.extension.Menu}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface MenuFinder {

    Mono<MenuVo> getByName(String name);

    Flux<MenuVo> getByNames(Collection<String> names);

    Mono<MenuVo> getPrimary();
}
