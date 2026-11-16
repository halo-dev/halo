package run.halo.app.theme.finders;

import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.Nullable;
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

    Flux<TagVo> getByNames(Collection<String> names);

    Mono<ListResult<TagVo>> list(@Nullable Integer page, @Nullable Integer size);

    /**
     * Converts already resolved tags to display values without fetching translations.
     *
     * <p>Public read boundaries must project source tags before calling this synchronous converter. Use the reactive
     * finder methods when loading tags for a theme.
     *
     * @param tags source or previously localized tags
     * @return tag values in the input order
     */
    List<TagVo> convertToVo(List<Tag> tags);

    Flux<TagVo> listAll();
}
