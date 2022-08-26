package run.halo.app.theme.finders.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import run.halo.app.core.extension.Tag;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
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
        return client.fetch(Tag.class, name)
            .map(TagVo::from)
            .block();
    }

    @Override
    public List<TagVo> getByNames(List<String> names) {
        return names.stream().map(this::getByName).toList();
    }

    @Override
    public ListResult<TagVo> list(int page, int size) {
        return client.list(Tag.class, null,
                DEFAULT_COMPARATOR.reversed(), Math.max(page - 1, 0), size)
            .map(list -> {
                List<TagVo> tagVos = list.stream()
                    .map(TagVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(list.getPage(), list.getSize(), list.getTotal(), tagVos);
            })
            .block();
    }

    @Override
    public List<TagVo> listAll() {
        return client.list(Tag.class, null,
                DEFAULT_COMPARATOR.reversed())
            .map(TagVo::from)
            .collectList()
            .block();
    }
}
