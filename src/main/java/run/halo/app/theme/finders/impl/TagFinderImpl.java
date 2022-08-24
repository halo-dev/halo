package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.app.theme.finders.SubscriberUtils;
import run.halo.app.theme.finders.TagFinder;
import run.halo.app.theme.finders.vo.TagVo;

/**
 * A default implementation of {@link TagFinder}.
 *
 * @author guqing
 * @since 2.0.0
 */
@Finder("tagFinder")
public class TagFinderImpl implements TagFinder {

    public static final Comparator<Tag> DEFAULT_COMPARATOR =
        Comparator.comparing(tag -> tag.getMetadata().getCreationTimestamp());

    private final ReactiveExtensionClient client;

    public TagFinderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }

    @Override
    public TagVo getByName(String name) {
        Mono<TagVo> mono = client.fetch(Tag.class, name)
            .map(TagVo::from);
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public ListResult<TagVo> list(int page, int size) {
        Mono<ListResult<TagVo>> mono = client.list(Tag.class, null,
                DEFAULT_COMPARATOR.reversed(), Math.max(page - 1, 0), size)
            .map(list -> {
                List<TagVo> tagVos = list.stream()
                    .map(TagVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), tagVos);
            });
        return SubscriberUtils.subscribe(mono);
    }

    @Override
    public List<TagVo> listAll() {
        Mono<List<TagVo>> mono = client.list(Tag.class, null,
                DEFAULT_COMPARATOR.reversed())
            .map(TagVo::from)
            .collectList();
        return SubscriberUtils.subscribe(mono);
    }
}
