package run.halo.app.theme.finders.impl;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.content.Tag;
import run.halo.app.extension.ExtensionUtil;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.PageRequest;
import run.halo.app.extension.PageRequestImpl;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.index.query.QueryFactory;
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
    public Mono<TagVo> getByName(String name) {
        return client.fetch(Tag.class, name)
            .map(TagVo::from);
    }

    @Override
    public Flux<TagVo> getByNames(Collection<String> names) {
        if (CollectionUtils.isEmpty(names)) {
            return Flux.empty();
        }
        var options = ListOptions.builder()
            .andQuery(QueryFactory.in("metadata.name", names))
            .build();
        return client.listAll(Tag.class, options, ExtensionUtil.defaultSort())
            .map(TagVo::from);
    }

    @Override
    public Mono<ListResult<TagVo>> list(Integer page, Integer size) {
        return listBy(new ListOptions(),
            PageRequestImpl.of(pageNullSafe(page), sizeNullSafe(size)));
    }

    @Override
    public List<TagVo> convertToVo(List<Tag> tags) {
        if (CollectionUtils.isEmpty(tags)) {
            return List.of();
        }
        return tags.stream()
            .map(TagVo::from)
            .collect(Collectors.toList());
    }

    @Override
    public Flux<TagVo> listAll() {
        return client.listAll(Tag.class, new ListOptions(),
                Sort.by(Sort.Order.desc("metadata.creationTimestamp")))
            .map(TagVo::from);
    }

    private Mono<ListResult<TagVo>> listBy(ListOptions listOptions, PageRequest pageRequest) {
        return client.listBy(Tag.class, listOptions, pageRequest)
            .map(result -> {
                List<TagVo> tagVos = result.get()
                    .map(TagVo::from)
                    .collect(Collectors.toList());
                return new ListResult<>(result.getPage(), result.getSize(), result.getTotal(),
                    tagVos);
            });
    }

    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }

    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
