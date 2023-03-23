package run.halo.app.theme.finders;

import java.util.List;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ListResult;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * A finder for {@link Tag}.
 *
 * @author guqing
 * @since 2.0.0
 */
public interface TagFinder {

    Mono<TagVo> getByName(String name);

    Flux<TagVo> getByNames(List<String> names);

    Mono<ListResult<TagVo>> list(@Nullable Integer page, @Nullable Integer size);

    Flux<TagVo> listAll();
}
