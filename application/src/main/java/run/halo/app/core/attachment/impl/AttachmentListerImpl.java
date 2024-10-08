package run.halo.app.core.attachment.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.core.attachment.AttachmentLister;
import run.halo.app.core.attachment.SearchRequest;
import run.halo.app.core.extension.attachment.Attachment;
import run.halo.app.core.extension.attachment.Group;
import run.halo.app.extension.ListOptions;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;

@Component
@RequiredArgsConstructor
public class AttachmentListerImpl implements AttachmentLister {
    private final ReactiveExtensionClient client;

    @Override
    public Mono<ListResult<Attachment>> listBy(SearchRequest searchRequest) {
        var groupListOptions = ListOptions.builder()
            .labelSelector()
            .exists(Group.HIDDEN_LABEL)
            .end()
            .build();
        return client.listAll(Group.class, groupListOptions, Sort.unsorted())
            .map(group -> group.getMetadata().getName())
            .collectList()
            .defaultIfEmpty(List.of())
            .flatMap(hiddenGroups -> client.listBy(Attachment.class,
                searchRequest.toListOptions(hiddenGroups),
                searchRequest.toPageRequest()
            ));
    }
}
